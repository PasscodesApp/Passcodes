package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure
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
import java.util.concurrent.Executors

import com.jeeldobariya.passcodes.autofill.AutofillRequestParser.AutofillFieldType
import com.jeeldobariya.passcodes.autofill.AutofillRequestParser.ParsedAutofillRequest

@RequiresApi(Build.VERSION_CODES.O)
class PasscodesAutofillService : AutofillService() {

    private val executor = Executors.newSingleThreadExecutor()
    private val presentations by lazy { AutofillPresentationFactory(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

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
        val usernameId = parsed.autofillId(AutofillFieldType.USERNAME)
        val passwordId = parsed.autofillId(AutofillFieldType.PASSWORD)
            ?: parsed.autofillId(AutofillFieldType.NEW_PASSWORD)

        // Not a login/account form we understand — decline politely.
        if (usernameId == null && passwordId == null) {
            callback.onSuccess(null)
            return
        }

        var cancelled = false
        cancellationSignal.setOnCancelListener { cancelled = true }

        val inlineRequest: InlineSuggestionsRequest? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) request.inlineSuggestionsRequest else null

        executor.execute {
            if (cancelled) return@execute

            // Explicit opt-in: if the site/app can't be identified, show
            // every saved credential and let the user pick. This is a
            // deliberate call for the fill picker (see filterPasswords'
            // default, which is the opposite for stricter callers).
            val matches = try {
                PasscodesDatabase(applicationContext)
                    .filterPasswords(parsed.webDomain, parsed.packageName, fallbackToAll = true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load credentials", e)
                emptyList()
            }

            if (cancelled) return@execute

            val responseBuilder = FillResponse.Builder()

            if (matches.isEmpty()) {
                // Nothing to offer, but still let the user save whatever
                // they type into this form.
                attachSaveInfo(responseBuilder, usernameId, passwordId)
                callback.onSuccess(responseBuilder.build())
                return@execute
            }

            matches.forEachIndexed { index, entry ->
                val inlineSpec = inlineRequest?.let { pickInlineSpec(it, index) }
                responseBuilder.addDataset(buildDataset(usernameId, passwordId, entry, inlineSpec))
            }

            attachSaveInfo(responseBuilder, usernameId, passwordId)
            callback.onSuccess(responseBuilder.build())
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val fillContext = request.fillContexts.lastOrNull()
        if (fillContext == null) {
            callback.onFailure("No fill context available")
            return
        }

        val parsed = AutofillRequestParser().parse(fillContext.structure)

        // Prefer a freshly-typed NEW_PASSWORD (signup / change-password
        // form); fall back to PASSWORD for an ordinary login form.
        val passwordValue = textValueOf(parsed.node(AutofillFieldType.NEW_PASSWORD))
            ?: textValueOf(parsed.node(AutofillFieldType.PASSWORD))

        if (passwordValue.isNullOrBlank()) {
            callback.onFailure("No password value to save")
            return
        }

        val username = textValueOf(parsed.node(AutofillFieldType.USERNAME)).orEmpty()
        val domain = parsed.webDomain ?: parsed.packageName ?: "unknown"
        val urlHint = buildUrlHint(parsed)

        executor.execute {
            val savedId = try {
                PasscodesDatabase(applicationContext).savePassword(
                    domain = domain,
                    username = username,
                    password = passwordValue,
                    url = urlHint
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save credentials", e)
                null
            }

            if (savedId != null) callback.onSuccess() else callback.onFailure("Could not save credentials")
        }
    }

    // -----------------------------------------------------------------

    private fun attachSaveInfo(builder: FillResponse.Builder, usernameId: AutofillId?, passwordId: AutofillId?) {
        val requiredId = passwordId ?: return
        val saveInfoBuilder = SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD, arrayOf(requiredId))
        usernameId?.let { saveInfoBuilder.setOptionalIds(arrayOf(it)) }
        builder.setSaveInfo(saveInfoBuilder.build())
    }

    private fun buildDataset(
        usernameId: AutofillId?,
        passwordId: AutofillId?,
        entry: PasscodesDatabase.PasswordEntry,
        inlineSpec: InlinePresentationSpec?
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        usernameId?.let { id ->
            presentations.applyValue(datasetBuilder, id, AutofillValue.forText(entry.username), entry, inlineSpec)
        }
        passwordId?.let { id ->
            presentations.applyValue(datasetBuilder, id, AutofillValue.forText(entry.password), entry, inlineSpec)
        }

        return datasetBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pickInlineSpec(inlineRequest: InlineSuggestionsRequest, index: Int): InlinePresentationSpec? {
        val specs = inlineRequest.inlinePresentationSpecs
        if (specs.isEmpty() || index >= inlineRequest.maxSuggestionCount) return null
        // Per the API contract, once specs run out the last one is reused.
        return specs[index.coerceAtMost(specs.size - 1)]
    }

    private fun textValueOf(node: AssistStructure.ViewNode?): String? =
        node?.autofillValue?.takeIf { it.isText }?.textValue?.toString()?.takeIf { it.isNotBlank() }

    private fun buildUrlHint(parsed: ParsedAutofillRequest): String? {
        val domain = parsed.webDomain ?: return null
        val scheme = parsed.webScheme ?: "https"
        return "$scheme://$domain"
    }

    companion object {
        private const val TAG = "PasscodesAutofillService"
    }
}
