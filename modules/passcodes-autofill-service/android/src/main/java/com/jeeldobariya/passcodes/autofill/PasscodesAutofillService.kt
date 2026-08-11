package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.os.Build
import android.os.CancellationSignal
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
        // 1. Get the latest AssistStructure from the fill context
        val context: FillContext = request.fillContexts.lastOrNull() ?: return callback.onSuccess(null)
        val structure: AssistStructure = context.structure

        // 2. Locate the focused field that requested autofill
        val autofillId = findFocusedAutofillId(structure.getWindowNodeAt(0).rootViewNode)
            ?: return callback.onSuccess(null)

        // 3. Build Response with 2 Swagged-out Datasets
        val responseBuilder = FillResponse.Builder()

        // --- Dataset 1: The Sass Master ---
        val views1 = RemoteViews(packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_icon, "😎")
            setTextViewText(R.id.autofill_title, "Idk baby, figure it out")
        }
        val dataset1 = Dataset.Builder()
            .setValue(
                autofillId,
                AutofillValue.forText("idk baby. your password you should remember it."),
                views1
            )
            .build()

        // --- Dataset 2: The Main Character ---
        val views2 = RemoteViews(packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_icon, "🔥")
            setTextViewText(R.id.autofill_title, "Not my vault, not my problem")
        }
        val dataset2 = Dataset.Builder()
            .setValue(
                autofillId,
                AutofillValue.forText("you really thought I had this saved? wild."),
                views2
            )
            .build()

        // 4. Attach datasets and deliver response
        val fillResponse = responseBuilder
            .addDataset(dataset1)
            .addDataset(dataset2)
            .build()

        callback.onSuccess(fillResponse)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // Acknowledge save request
        callback.onSuccess()
    }

    /**
     * Recursively traverses the view tree to find the node currently focused by the user.
     */
    private fun findFocusedAutofillId(node: AssistStructure.ViewNode): AutofillId? {
        if (node.isFocused && node.autofillId != null) {
            return node.autofillId
        }

        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i)
            val id = findFocusedAutofillId(child)
            if (id != null) return id
        }

        return null
    }
}
