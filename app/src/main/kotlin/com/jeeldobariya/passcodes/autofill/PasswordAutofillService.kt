package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.data.Passcode
import com.jeeldobariya.passcodes.data.PasscodeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PasswordAutofillService : AutofillService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val context = request.fillContexts
        val structure = context.last().structure

        val viewNodes = mutableMapOf<String, AssistStructure.ViewNode>()
        parseStructure(structure.getWindowNodeAt(0).rootViewNode, viewNodes)

        val usernameNode = viewNodes["username"] ?: viewNodes["emailAddress"]
        val passwordNode = viewNodes["password"]

        if (usernameNode?.autofillId == null || passwordNode?.autofillId == null) {
            callback.onSuccess(null)
            return
        }

        val usernameId = usernameNode.autofillId!!
        val passwordId = passwordNode.autofillId!!

        cancellationSignal.setOnCancelListener {
            // Handle cancellation
        }

        serviceScope.launch {
            val passcodes = PasscodeDatabase.getDatabase(applicationContext).passcodeDao().getAllPasscodes().first()
            val responseBuilder = FillResponse.Builder()

            for (passcode in passcodes) {
                val presentation = RemoteViews(packageName, R.layout.autofill_list_item).apply {
                    setTextViewText(R.id.autofill_username, passcode.name)
                }

                val dataset = android.service.autofill.Dataset.Builder(presentation)
                    .setValue(usernameId, AutofillValue.forText(passcode.name))
                    .setValue(passwordId, AutofillValue.forText(passcode.value))
                    .build()
                responseBuilder.addDataset(dataset)
            }

            callback.onSuccess(responseBuilder.build())
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val context = request.fillContexts
        val structure = context.last().structure

        val viewNodes = mutableMapOf<String, AssistStructure.ViewNode>()
        parseStructure(structure.getWindowNodeAt(0).rootViewNode, viewNodes)

        val usernameNode = viewNodes["username"] ?: viewNodes["emailAddress"]
        val passwordNode = viewNodes["password"]

        val username = usernameNode?.text?.toString()
        val password = passwordNode?.text?.toString()

        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            serviceScope.launch {
                PasscodeDatabase.getDatabase(applicationContext).passcodeDao().insert(
                    Passcode(name = username, value = password)
                )
            }
            callback.onSuccess()
        } else {
            callback.onFailure("Could not save credentials.")
        }
    }

    private fun parseStructure(
        node: AssistStructure.ViewNode,
        viewNodes: MutableMap<String, AssistStructure.ViewNode>
    ) {
        node.autofillHints?.forEach { hint ->
            if (!viewNodes.containsKey(hint)) {
                viewNodes[hint] = node
            }
        }

        for (i in 0 until node.childCount) {
            parseStructure(node.getChildAt(i), viewNodes)
        }
    }
}
