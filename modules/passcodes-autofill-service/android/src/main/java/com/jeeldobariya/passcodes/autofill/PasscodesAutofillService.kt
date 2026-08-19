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
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class PasscodesAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        cancellationSignal.setOnCancelListener {
            // No cancellable background work at the moment.
        }

        val fillContext = request.fillContexts.lastOrNull()
            ?: return callback.onSuccess(null)

        val structure = fillContext.structure

        val viewNodes = mutableMapOf<String, AssistStructure.ViewNode>()

        parseStructure(
            node = structure.getWindowNodeAt(0).rootViewNode,
            viewNodes = viewNodes
        )

        /*
         * Android applications and HTML forms can expose different
         * autofill hints for the same logical field.
         *
         * Username:
         *   username
         *   emailAddress
         *
         * Password:
         *   password
         *   current-password
         *   new-password
         */
        val usernameNode =
            viewNodes[AUTOFILL_HINT_USERNAME]
                ?: viewNodes[AUTOFILL_HINT_EMAIL_ADDRESS]

        val passwordNode =
            viewNodes[AUTOFILL_HINT_PASSWORD]
                ?: viewNodes[AUTOFILL_HINT_CURRENT_PASSWORD]
                ?: viewNodes[AUTOFILL_HINT_NEW_PASSWORD]

        /*
         * If this isn't a login/account form that we understand,
         * don't return any datasets.
         */
        if (usernameNode == null && passwordNode == null) {
            return callback.onSuccess(null)
        }

        val usernameId = usernameNode?.autofillId
        val passwordId = passwordNode?.autofillId

        if (usernameId == null && passwordId == null) {
            return callback.onSuccess(null)
        }

        if (cancellationSignal.isCanceled) {
            return
        }

        val passwords = PasscodesDatabase(applicationContext)
            .getAllPasswords()

        if (passwords.isEmpty()) {
            callback.onSuccess(null)
            return
        }

        val responseBuilder = FillResponse.Builder()

        passwords.forEach { password ->
            responseBuilder.addDataset(
                createDataset(
                    usernameId = usernameId,
                    passwordId = passwordId,
                    password = password
                )
            )
        }

        callback.onSuccess(
            responseBuilder.build()
        )
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: SaveCallback
    ) {
        /*
         * Saving credentials is intentionally not implemented yet.
         *
         * The current Autofill implementation only reads existing
         * credentials from Passcodes.
         */
        callback.onSuccess()
    }

    /**
     * Recursively walks the AssistStructure and indexes ViewNodes
     * by their Autofill hints.
     */
    private fun parseStructure(
        node: AssistStructure.ViewNode,
        viewNodes: MutableMap<String, AssistStructure.ViewNode>
    ) {
        node.autofillHints?.forEach { hint ->
            viewNodes.putIfAbsent(hint, node)
        }

        for (index in 0 until node.childCount) {
            parseStructure(
                node = node.getChildAt(index),
                viewNodes = viewNodes
            )
        }
    }

    /**
     * Creates a Dataset containing whichever login fields were
     * exposed by the current application / web page.
     *
     * If both username and password fields exist:
     *
     *     username -> username
     *     password -> password
     *
     * If only one exists, only that field is populated.
     */
    private fun createDataset(
        usernameId: AutofillId?,
        passwordId: AutofillId?,
        password: PasscodesDatabase.PasswordEntry
    ): Dataset {

        val presentation = RemoteViews(
            packageName,
            R.layout.autofill_item
        ).apply {
            setImageViewResource(
                R.id.autofill_icon,
                R.drawable.ic_autofill_lock
            )

            setTextViewText(
                R.id.autofill_domain,
                password.domain
            )

            setTextViewText(
                R.id.autofill_username,
                password.username
            )
        }

        val datasetBuilder = Dataset.Builder()

        usernameId?.let { id ->
            datasetBuilder.setValue(
                id,
                AutofillValue.forText(password.username),
                presentation
            )
        }

        passwordId?.let { id ->
            datasetBuilder.setValue(
                id,
                AutofillValue.forText(password.password),
                presentation
            )
        }

        return datasetBuilder.build()
    }

    companion object {

        /**
         * Android Autofill hint:
         * A username / account identifier.
         */
        private const val AUTOFILL_HINT_USERNAME = "username"

        /**
         * Android Autofill hint:
         * An email address.
         */
        private const val AUTOFILL_HINT_EMAIL_ADDRESS = "emailAddress"

        /**
         * Android Autofill hint:
         * A generic password.
         */
        private const val AUTOFILL_HINT_PASSWORD = "password"

        /**
         * W3C autocomplete value:
         * The password used to authenticate an existing account.
         */
        private const val AUTOFILL_HINT_CURRENT_PASSWORD = "current-password"

        /**
         * W3C autocomplete value:
         * A new password being created or changed.
         */
        private const val AUTOFILL_HINT_NEW_PASSWORD = "new-password"
    }
}
