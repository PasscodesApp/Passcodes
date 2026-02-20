package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.jeeldobariya.passcodes.autofill.data.Passcode
import com.jeeldobariya.passcodes.autofill.data.PasscodeDatabase
import com.jeeldobariya.passcodes.database.master.PasswordsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

// TODO: currently the code here serves as a foundation for autofill features..
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
            val passwordsDao by inject<PasswordsDao>()
            val passwords = passwordsDao.getAllPasswords().first()
            val responseBuilder = FillResponse.Builder()

            if (passwords.isEmpty()) {
                callback.onSuccess(null)
            }

            for (password in passwords) {
                val presentation = RemoteViews(packageName, R.layout.autofill_list_item).apply {
                    setTextViewText(
                        R.id.autofill_username,
                        "${password.domain}(${password.username})"
                    )
                }

                val dataset = android.service.autofill.Dataset.Builder(presentation)
                    .setValue(usernameId, AutofillValue.forText(password.username))
                    .setValue(passwordId, AutofillValue.forText(password.password))
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
            callback.onFailure(getString(R.string.could_not_save_credentials))
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
