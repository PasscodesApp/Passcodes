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
            // The current implementation does not have any cancellable
            // background work to clean up.
        }

        val fillContext = request.fillContexts.lastOrNull()
            ?: return callback.onSuccess(null)

        val rootNode = fillContext.structure
            .getWindowNodeAt(0)
            .rootViewNode

        val viewNodes = mutableMapOf<String, AssistStructure.ViewNode>()

        parseStructure(
            node = rootNode,
            viewNodes = viewNodes
        )

        /*
         * Only provide suggestions when Android has identified
         * a password field.
         */
        val passwordNode = viewNodes[AUTOFILL_HINT_PASSWORD]
            ?: return callback.onSuccess(null)

        val passwordId = passwordNode.autofillId
            ?: return callback.onSuccess(null)

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
         * This service currently only provides existing credentials
         * from Passcodes.
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
     * Creates an Autofill Dataset for a single Passcodes entry.
     */
    private fun createDataset(
        passwordId: AutofillId,
        password: PasscodesDatabase.PasswordEntry
    ): Dataset {

        val presentation = RemoteViews(
            packageName,
            R.layout.autofill_item
        ).apply {
            setTextViewText(
                R.id.autofill_icon,
                "🔐"
            )

            setTextViewText(
                R.id.autofill_title,
                "${password.domain} (${password.username})"
            )
        }

        return Dataset.Builder()
            .setValue(
                passwordId,
                AutofillValue.forText(password.password),
                presentation
            )
            .build()
    }

    companion object {
        private const val AUTOFILL_HINT_PASSWORD = "password"
    }
}
