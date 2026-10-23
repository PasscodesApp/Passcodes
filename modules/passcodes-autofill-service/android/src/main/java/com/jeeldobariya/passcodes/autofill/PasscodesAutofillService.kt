package com.jeeldobariya.passcodes.autofill

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
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
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

import com.jeeldobariya.passcodes.autofill.AutofillRequestParser.AutofillFieldType
import com.jeeldobariya.passcodes.autofill.AutofillRequestParser.ParsedAutofillRequest

/**
 * Android Autofill entry point for Passcodes.
 *
 * Both callbacks follow the same shape: parse the on-screen structure
 * into the fields we care about (see [AutofillRequestParser]), then do
 * the actual database/biometric work off the main thread via [executor].
 */
@RequiresApi(Build.VERSION_CODES.O)
class PasscodesAutofillService : AutofillService() {

    private val executor = Executors.newSingleThreadExecutor()
    private val presentations by lazy { AutofillPresentationFactory(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
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

        // Not a login/account form we understand — decline politely.
        if (fields.isEmpty) {
            callback.onSuccess(null)
            return
        }

        val cancelled = AtomicBoolean(false)
        cancellationSignal.setOnCancelListener { cancelled.set(true) }

        executor.execute {
            if (cancelled.get()) return@execute
            // An exception escaping an executor task would take the
            // whole app process down (and the RN app shares it), so
            // anything unexpected degrades to "no suggestions".
            try {
                handleFillRequest(request, fields, parsed, cancelled = { cancelled.get() }, callback)
            } catch (t: Throwable) {
                Log.e(TAG, "Fill request failed", t)
                runCatching { callback.onSuccess(null) }
            }
        }
    }

    private fun handleFillRequest(
        request: FillRequest,
        fields: LoginFieldIds,
        parsed: ParsedAutofillRequest,
        cancelled: () -> Boolean,
        callback: FillCallback
    ) {
        val inlineRequest = resolveInlineSuggestionsRequest(request)
        val biometricGateActive = resolveBiometricGateActive()
        val fillMode = AutofillPreferences.getFillModeBlocking(applicationContext)

        val matches = try {
            selectCredentials(fillMode, parsed).take(MAX_DATASETS)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load credentials", e)
            emptyList()
        }

        if (cancelled()) return

        val responseBuilder = FillResponse.Builder()

        matches.forEachIndexed { index, entry ->
            val inlineSpec = inlineRequest?.let { pickInlineSpec(it, index) }
            val dataset = if (biometricGateActive) {
                buildLockedDataset(fields, entry, inlineSpec)
            } else {
                buildDataset(fields, entry, inlineSpec)
            }
            responseBuilder.addDataset(dataset)
        }

        // Even with nothing to offer, still let the user save whatever
        // they type into this form.
        val hasSaveInfo = attachSaveInfo(responseBuilder, fields)

        // FillResponse.Builder#build() throws if it holds neither a
        // dataset nor SaveInfo — answer "nothing here" instead.
        if (matches.isEmpty() && !hasSaveInfo) {
            callback.onSuccess(null)
            return
        }

        callback.onSuccess(responseBuilder.build())
    }

    /** Picks which saved credentials to offer, per the user's fill-mode setting. */
    private fun selectCredentials(
        mode: FillMode,
        parsed: ParsedAutofillRequest
    ): List<PasscodesDatabase.PasswordEntry> {
        val db = PasscodesDatabase(applicationContext)
        return when (mode) {
            FillMode.SMART -> db.filterPasswords(parsed.webDomain, parsed.packageName, fallbackToAll = false)
            FillMode.AUTO -> db.filterPasswords(parsed.webDomain, parsed.packageName, fallbackToAll = true)
            FillMode.DUMB -> db.getAllPasswords()
        }
    }

    /** Honors the user's inline-suggestions preference on top of OS/API support. */
    private fun resolveInlineSuggestionsRequest(request: FillRequest): InlineSuggestionsRequest? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val enabled = AutofillPreferences.isInlineSuggestionsEnabledBlocking(applicationContext)
        return if (enabled) request.inlineSuggestionsRequest else null
    }

    /**
     * The biometric gate only activates when BOTH the user wants it
     * (preference, default on) AND the device can actually satisfy it
     * (something enrolled). A device with no lock screen security at
     * all never blocks the user from their own vault — there's nothing
     * meaningful to gate behind.
     */
    private fun resolveBiometricGateActive(): Boolean {
        val preferenceEnabled = AutofillPreferences.isBiometricLockEnabledBlocking(applicationContext)
        if (!preferenceEnabled) return false

        val canAuthenticate = BiometricManager.from(applicationContext)
            .canAuthenticate(AutofillUnlockActivity.ALLOWED_AUTHENTICATORS)
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** Returns true if SaveInfo was attached (needs a password field to key off). */
    private fun attachSaveInfo(builder: FillResponse.Builder, fields: LoginFieldIds): Boolean {
        val requiredId = fields.passwordId ?: return false
        val saveInfoBuilder = SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD, arrayOf(requiredId))
        fields.usernameId?.let { saveInfoBuilder.setOptionalIds(arrayOf(it)) }
        builder.setSaveInfo(saveInfoBuilder.build())
        return true
    }

    private fun buildDataset(
        fields: LoginFieldIds,
        entry: PasscodesDatabase.PasswordEntry,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        fields.usernameId?.let { id ->
            presentations.applyValue(datasetBuilder, id, AutofillValue.forText(entry.username), entry, inlineSpec)
        }
        fields.passwordId?.let { id ->
            presentations.applyValue(datasetBuilder, id, AutofillValue.forText(entry.password), entry, inlineSpec)
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
        entry: PasscodesDatabase.PasswordEntry,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        fields.usernameId?.let { presentations.applyLockedPlaceholder(datasetBuilder, it, entry, inlineSpec) }
        fields.passwordId?.let { presentations.applyLockedPlaceholder(datasetBuilder, it, entry, inlineSpec) }

        datasetBuilder.setAuthentication(buildUnlockPendingIntent(entry, fields).intentSender)
        return datasetBuilder.build()
    }

    private fun buildUnlockPendingIntent(entry: PasscodesDatabase.PasswordEntry, fields: LoginFieldIds): PendingIntent {
        val intent = Intent(applicationContext, AutofillUnlockActivity::class.java).apply {
            putExtra(AutofillUnlockActivity.EXTRA_ROW_ID, entry.id)
            fields.usernameId?.let { putExtra(AutofillUnlockActivity.EXTRA_USERNAME_AUTOFILL_ID, it) }
            fields.passwordId?.let { putExtra(AutofillUnlockActivity.EXTRA_PASSWORD_AUTOFILL_ID, it) }
        }

        // Request code unique per row so Android never collapses two
        // different rows' unlock intents into one stale PendingIntent.
        return PendingIntent.getActivity(
            applicationContext,
            entry.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

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

        executor.execute {
            try {
                handleSaveRequest(parsed, username, passwordValue, callback)
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
    }
}
