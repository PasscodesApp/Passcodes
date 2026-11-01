package com.jeeldobariya.passcodes.autofill.service

import com.jeeldobariya.passcodes.autofill.identity.FillValue
import com.jeeldobariya.passcodes.autofill.identity.IdentityDatasetPlanner
import com.jeeldobariya.passcodes.autofill.identity.IdentityFiller
import com.jeeldobariya.passcodes.autofill.identity.IdentityPartition
import com.jeeldobariya.passcodes.autofill.identity.PlannedDataset
import com.jeeldobariya.passcodes.autofill.identity.PlannedField
import com.jeeldobariya.passcodes.autofill.information.AutofillInformationStore
import com.jeeldobariya.passcodes.autofill.parser.AutofillRequestParser
import com.jeeldobariya.passcodes.autofill.parser.AutofillValues
import com.jeeldobariya.passcodes.autofill.presentation.AutofillPresentationFactory
import com.jeeldobariya.passcodes.autofill.presentation.AutofillSaveUiFactory
import com.jeeldobariya.passcodes.autofill.presentation.DatasetDisplay
import com.jeeldobariya.passcodes.autofill.security.DeviceSecurity
import com.jeeldobariya.passcodes.autofill.unlock.IdentityUnlockExtras
import com.jeeldobariya.passcodes.autofill.settings.AutofillPreferences
import com.jeeldobariya.passcodes.autofill.settings.FillMode
import com.jeeldobariya.passcodes.autofill.unlock.AutofillUnlockActivity
import com.jeeldobariya.passcodes.database.PasscodesDatabase
import com.jeeldobariya.passcodes.database.model.PasswordEntry
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

import com.jeeldobariya.passcodes.autofill.parser.AutofillRequestParser.AutofillFieldType
import com.jeeldobariya.passcodes.autofill.parser.AutofillRequestParser.IdentityField
import com.jeeldobariya.passcodes.autofill.parser.AutofillRequestParser.ParsedAutofillRequest

/**
 * Android Autofill entry point for Passcodes.
 *
 * Both callbacks follow the same shape: parse the on-screen structure
 * into the fields we care about (see [AutofillRequestParser]), then do
 * the actual database/preferences work off the main thread on [serviceScope].
 */
@RequiresApi(Build.VERSION_CODES.O)
class PasscodesAutofillService : AutofillService() {

    // One request at a time, off the main thread — the same ordering the
    // old single-thread executor gave, but cancellable and structured.
    // SupervisorJob: one failed request never cancels its siblings.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val presentations by lazy { AutofillPresentationFactory(applicationContext) }
    private val saveUi by lazy { AutofillSaveUiFactory(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        // Cancels in-flight fill requests. Saves are shielded from this
        // (see handleSaveRequest) so a typed password is never dropped.
        serviceScope.cancel()
    }

    // -----------------------------------------------------------------
    // Fill
    // -----------------------------------------------------------------

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val fillContext = request.fillContexts.lastOrNull()
        if (fillContext == null) {
            callback.onSuccess(null)
            return
        }

        val parsed = AutofillRequestParser().parse(fillContext.structure)
        val fields = LoginFieldIds.from(parsed)
        val manualRequest = request.flags and FillRequest.FLAG_MANUAL_REQUEST != 0
        val identityFields = identityFieldsToOffer(parsed, manualRequest)

        // Neither a login form nor personal details we recognise — decline politely.
        if (fields.isEmpty && identityFields.isEmpty()) {
            callback.onSuccess(null)
            return
        }

        val job = serviceScope.launch {
            // An exception escaping a coroutine would take the whole app
            // process down (and the RN app shares it), so anything
            // unexpected degrades to "no suggestions".
            try {
                handleFillRequest(request, fields, identityFields, parsed, callback)
            } catch (e: CancellationException) {
                throw e // cancelled by the system: it no longer wants a reply
            } catch (t: Throwable) {
                Log.e(TAG, "Fill request failed", t)
                runCatching { callback.onSuccess(null) }
            }
        }

        // The system cancels a fill request when the user moves on.
        cancellationSignal.setOnCancelListener { job.cancel() }
    }

    /**
     * Which personal-detail fields may be offered on this screen.
     *
     *  - Sign-in screens get saved logins only (a manual request can override).
     *  - Address and birth date are never offered on a page that mixes
     *    origins (a third-party iframe) or isn't HTTPS.
     */
    private fun identityFieldsToOffer(parsed: ParsedAutofillRequest, manualRequest: Boolean): List<IdentityField> {
        if (parsed.isLoginForm && !manualRequest) return emptyList()

        val resolved = parsed.resolveIdentityFields(manualRequest)
        val sensitiveAllowed = parsed.distinctWebDomains.size <= 1 && !parsed.webScheme.equals("http", ignoreCase = true)
        return if (sensitiveAllowed) resolved else resolved.filter { it.spec.kind.partition == IdentityPartition.CONTACT }
    }

    private suspend fun handleFillRequest(
        request: FillRequest,
        fields: LoginFieldIds,
        identityFields: List<IdentityField>,
        parsed: ParsedAutofillRequest,
        callback: FillCallback
    ) {
        val prefs = AutofillPreferences.snapshot(applicationContext) // one DataStore read
        val inlineRequest = resolveInlineSuggestionsRequest(request, prefs.inlineSuggestionsEnabled)
        val biometricGateActive = resolveBiometricGateActive(prefs.biometricLockEnabled)
        val destination = resolveSiteLabel(parsed)

        val matches = if (fields.isEmpty) {
            emptyList()
        } else {
            try {
                // A locked row never shows or carries the password (it is
                // re-read by id after unlock), so don't load it at all.
                selectCredentials(prefs.fillMode, parsed, includePassword = !biometricGateActive).take(MAX_DATASETS)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load credentials", e)
                emptyList()
            }
        }

        val information = if (identityFields.isEmpty() || !DeviceSecurity.isDeviceSecure(applicationContext)) {
            null // no personal details without a secure lock screen
        } else {
            AutofillInformationStore.get(applicationContext)
        }

        currentCoroutineContext().ensureActive()

        val responseBuilder = FillResponse.Builder()
        var slot = 0
        var offered = 0

        matches.forEach { entry ->
            val inlineSpec = inlineRequest?.let { pickInlineSpec(it, slot++) }
            val display = presentations.displayForCredential(entry, biometricGateActive, nextRequestCode())
            val dataset = if (biometricGateActive) {
                buildLockedDataset(fields, entry, display, destination, inlineSpec)
            } else {
                buildDataset(fields, entry, display, inlineSpec)
            }
            responseBuilder.addDataset(dataset)
            offered++
        }

        if (information != null && !information.isEmpty) {
            val planned = IdentityDatasetPlanner.plan(
                info = information,
                fields = identityFields.map { PlannedField(it.autofillId, it.spec) },
                maxDatasets = MAX_IDENTITY_DATASETS
            )
            for (plan in planned) {
                val inlineSpec = inlineRequest?.let { pickInlineSpec(it, slot++) }
                val dataset = if (biometricGateActive) {
                    buildLockedIdentityDataset(plan, destination, inlineSpec)
                } else {
                    buildIdentityDataset(plan, information, inlineSpec)
                }
                responseBuilder.addDataset(dataset)
                offered++
            }
        }

        // Even with nothing to offer, still let the user save whatever
        // they type into this form.
        val hasSaveInfo = attachSaveInfo(responseBuilder, fields, destination)

        // FillResponse.Builder#build() throws if it holds neither a
        // dataset nor SaveInfo — answer "nothing here" instead.
        if (offered == 0 && !hasSaveInfo) {
            callback.onSuccess(null)
            return
        }

        callback.onSuccess(responseBuilder.build())
    }

    /** Picks which saved credentials to offer, per the user's fill-mode setting. */
    private fun selectCredentials(
        mode: FillMode,
        parsed: ParsedAutofillRequest,
        includePassword: Boolean
    ): List<PasswordEntry> {
        val db = PasscodesDatabase(applicationContext)
        return when (mode) {
            FillMode.SMART -> db.filterPasswords(parsed.webDomain, parsed.packageName, fallbackToAll = false, includePassword = includePassword)
            FillMode.AUTO -> db.filterPasswords(parsed.webDomain, parsed.packageName, fallbackToAll = true, includePassword = includePassword)
            FillMode.DUMB -> db.getAllPasswords(includePassword = includePassword)
        }
    }

    /** Honors the user's inline-suggestions preference on top of OS/API support. */
    private fun resolveInlineSuggestionsRequest(request: FillRequest, enabled: Boolean): InlineSuggestionsRequest? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return if (enabled) request.inlineSuggestionsRequest else null
    }

    /**
     * The biometric gate only activates when BOTH the user wants it
     * (preference, default off) AND the device can actually satisfy it
     * (something enrolled). A device with no lock screen security at
     * all never blocks the user from their own vault — there's nothing
     * meaningful to gate behind.
     */
    private fun resolveBiometricGateActive(preferenceEnabled: Boolean): Boolean {
        if (!preferenceEnabled) return false

        val canAuthenticate = BiometricManager.from(applicationContext)
            .canAuthenticate(AutofillUnlockActivity.ALLOWED_AUTHENTICATORS)
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** Returns true if SaveInfo was attached (needs a password field to key off). */
    private fun attachSaveInfo(builder: FillResponse.Builder, fields: LoginFieldIds, siteLabel: String): Boolean {
        val requiredId = fields.passwordId ?: return false
        val saveInfoBuilder = SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD, arrayOf(requiredId))
        fields.usernameId?.let { saveInfoBuilder.setOptionalIds(arrayOf(it)) }

        // CustomDescription (API 27+) swaps the plain "Save password?"
        // text for our own card showing the site and the typed username
        // (the password stays masked). Older versions keep the default.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            saveInfoBuilder.setCustomDescription(saveUi.buildDescription(siteLabel, fields.usernameId))
        }
        builder.setSaveInfo(saveInfoBuilder.build())
        return true
    }

    private fun buildDataset(
        fields: LoginFieldIds,
        entry: PasswordEntry,
        display: DatasetDisplay,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        fields.usernameId?.let { id ->
            presentations.applyValue(datasetBuilder, id, AutofillValue.forText(entry.username), display, inlineSpec)
        }
        fields.passwordId?.let { id ->
            presentations.applyValue(datasetBuilder, id, AutofillValue.forText(entry.password), display, inlineSpec)
        }

        return datasetBuilder.build()
    }

    /**
     * Builds a dataset with no real values attached — only a "tap to
     * unlock" presentation — guarded by setAuthentication so the system
     * launches AutofillUnlockActivity when the user taps it. The real
     * values are supplied only after that Activity confirms biometric/
     * device-credential authentication.
     *
     * [inlineSpec] is non-null only when the user left inline
     * suggestions on, in which case the locked row also shows up as a
     * keyboard chip (domain + "Tap to unlock", never the username).
     */
    private fun buildLockedDataset(
        fields: LoginFieldIds,
        entry: PasswordEntry,
        display: DatasetDisplay,
        destination: String,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        fields.usernameId?.let { presentations.applyLockedPlaceholder(datasetBuilder, it, display, inlineSpec) }
        fields.passwordId?.let { presentations.applyLockedPlaceholder(datasetBuilder, it, display, inlineSpec) }

        val pendingIntent = createUnlockPendingIntent {
            putExtra(AutofillUnlockActivity.EXTRA_ROW_ID, entry.id)
            putExtra(AutofillUnlockActivity.EXTRA_DESTINATION, destination)
            fields.usernameId?.let { putExtra(AutofillUnlockActivity.EXTRA_USERNAME_AUTOFILL_ID, it) }
            fields.passwordId?.let { putExtra(AutofillUnlockActivity.EXTRA_PASSWORD_AUTOFILL_ID, it) }
        }
        datasetBuilder.setAuthentication(pendingIntent.intentSender)
        return datasetBuilder.build()
    }

    /** Personal details, unlocked: the real values go straight into the dataset. */
    private fun buildIdentityDataset(
        plan: PlannedDataset<AutofillId>,
        information: com.jeeldobariya.passcodes.autofill.information.AutofillInformation,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()
        val display = presentations.displayForIdentity(plan.kind, plan.summary, locked = false, requestKey = nextRequestCode())

        for (field in plan.fields) {
            val value = IdentityFiller.resolve(information, plan.selection, field.spec) ?: continue
            presentations.applyValue(datasetBuilder, field.id, value.toAutofillValue(), display, inlineSpec)
        }
        return datasetBuilder.build()
    }

    /**
     * Personal details, locked: placeholders only. The intent carries
     * which fields to fill and how — never the values (see
     * [IdentityUnlockExtras]).
     */
    private fun buildLockedIdentityDataset(
        plan: PlannedDataset<AutofillId>,
        destination: String,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()
        val display = presentations.displayForIdentity(plan.kind, plan.summary, locked = true, requestKey = nextRequestCode())

        for (field in plan.fields) {
            presentations.applyLockedPlaceholder(datasetBuilder, field.id, display, inlineSpec)
        }

        val pendingIntent = createUnlockPendingIntent {
            putExtra(AutofillUnlockActivity.EXTRA_IS_IDENTITY, true)
            putExtra(AutofillUnlockActivity.EXTRA_DESTINATION, destination)
            IdentityUnlockExtras.write(this, plan.fields, plan.selection)
        }
        datasetBuilder.setAuthentication(pendingIntent.intentSender)
        return datasetBuilder.build()
    }

    private fun FillValue.toAutofillValue(): AutofillValue = when (this) {
        is FillValue.Text -> AutofillValue.forText(value)
        is FillValue.ListIndex -> AutofillValue.forList(index)
        is FillValue.DateMillis -> AutofillValue.forDate(millis)
    }

    /**
     * Every unlock PendingIntent gets its own request code AND its own
     * data URI. PendingIntents are matched on the Intent minus its
     * extras, so two suggestions that differed only in extras (same row,
     * different fields) would otherwise collapse into one — and
     * FLAG_UPDATE_CURRENT would then overwrite the first's fields with
     * the second's, filling the wrong inputs.
     */
    private fun createUnlockPendingIntent(configure: Intent.() -> Unit): PendingIntent {
        val code = nextRequestCode()
        val intent = Intent(applicationContext, AutofillUnlockActivity::class.java).apply {
            data = Uri.parse("passcodes-autofill://unlock/$code")
            configure()
        }
        return PendingIntent.getActivity(
            applicationContext,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextRequestCode(): Int = requestCodes.incrementAndGet()

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pickInlineSpec(inlineRequest: InlineSuggestionsRequest, index: Int): InlinePresentationSpec? {
        val specs = inlineRequest.inlinePresentationSpecs
        if (specs.isEmpty() || index >= inlineRequest.maxSuggestionCount) return null
        // Per the API contract, once specs run out the last one is reused.
        return specs[index.coerceAtMost(specs.size - 1)]
    }

    // -----------------------------------------------------------------
    // Save
    // -----------------------------------------------------------------

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val fillContext = request.fillContexts.lastOrNull()
        if (fillContext == null) {
            callback.onFailure("No fill context available")
            return
        }

        val parsed = AutofillRequestParser().parse(fillContext.structure)

        // Prefer a freshly-typed NEW_PASSWORD (signup / change-password
        // form); fall back to PASSWORD for an ordinary login form.
        val passwordValue = AutofillValues.textOf(parsed.node(AutofillFieldType.NEW_PASSWORD))
            ?: AutofillValues.textOf(parsed.node(AutofillFieldType.PASSWORD))

        if (passwordValue.isNullOrBlank()) {
            callback.onFailure("No password value to save")
            return
        }

        val username = AutofillValues.textOf(parsed.node(AutofillFieldType.USERNAME)).orEmpty()

        serviceScope.launch {
            try {
                // NonCancellable: the user has already confirmed the save,
                // so even if the service is torn down mid-request the
                // write runs to completion instead of losing the password.
                withContext(NonCancellable) {
                    handleSaveRequest(parsed, username, passwordValue, callback)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Save request failed", t)
                runCatching { callback.onFailure("Could not save credentials") }
            }
        }
    }

    private fun handleSaveRequest(
        parsed: ParsedAutofillRequest,
        username: String,
        password: String,
        callback: SaveCallback
    ) {
        // A web domain means this is a browser/WebView form: the "app"
        // (e.g. Chrome) isn't the credential's owner, so we don't tag
        // applicationIds and we don't need an app label for `domain`.
        // No web domain means this is a genuine native-app login form,
        // so the app itself becomes both the domain label and the
        // applicationIds entry to merge in.
        val isNativeAppForm = parsed.webDomain == null
        val appLabel = if (isNativeAppForm) resolveAppLabel(parsed.packageName) else null
        val packageIdToMerge = if (isNativeAppForm) parsed.packageName else null

        val domain = parsed.webDomain ?: appLabel ?: parsed.packageName ?: "unknown"
        val urlHint = buildUrlHint(parsed)

        val savedId = try {
            PasscodesDatabase(applicationContext).savePassword(
                domain = domain,
                username = username,
                password = password,
                url = urlHint,
                packageId = packageIdToMerge
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save credentials", e)
            null
        }

        if (savedId != null) callback.onSuccess() else callback.onFailure("Could not save credentials")
    }

    /** What to call this form in the UI: the web domain, else the app's name. */
    private fun resolveSiteLabel(parsed: ParsedAutofillRequest): String =
        parsed.webDomain
            ?: resolveAppLabel(parsed.packageName)
            ?: parsed.packageName
            ?: ""

    /** Best-effort app display name lookup, e.g. "com.outlook.android" -> "Outlook". */
    private fun resolveAppLabel(packageName: String?): String? {
        if (packageName.isNullOrBlank()) return null
        return try {
            val pm = applicationContext.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo)?.toString()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildUrlHint(parsed: ParsedAutofillRequest): String? {
        val domain = parsed.webDomain ?: return null
        val scheme = parsed.webScheme ?: "https"
        return "$scheme://$domain"
    }

    /** Groups the two autofill ids an onscreen login form cares about. */
    private data class LoginFieldIds(val usernameId: AutofillId?, val passwordId: AutofillId?) {
        val isEmpty: Boolean get() = usernameId == null && passwordId == null

        companion object {
            fun from(parsed: ParsedAutofillRequest): LoginFieldIds {
                val usernameId = parsed.autofillId(AutofillFieldType.USERNAME)
                val passwordId = parsed.autofillId(AutofillFieldType.PASSWORD)
                    ?: parsed.autofillId(AutofillFieldType.NEW_PASSWORD)
                return LoginFieldIds(usernameId, passwordId)
            }
        }
    }

    companion object {
        private const val TAG = "PasscodesAutofillService"

        // Datasets travel to the system over Binder (~1 MB shared
        // transaction limit, each one carrying RemoteViews), so a huge
        // vault in "dumb" mode must not be sent whole. Rows are already
        // ordered most-recently-updated first.
        private const val MAX_DATASETS = 20

        /** Personal-detail suggestions per screen (on top of saved logins). */
        private const val MAX_IDENTITY_DATASETS = 6

        private val requestCodes = AtomicInteger()
    }
}
