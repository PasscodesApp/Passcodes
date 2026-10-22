package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure

/** Reads the current text value a user typed into an Autofill form field. */
object AutofillValues {
    fun textOf(node: AssistStructure.ViewNode?): String? =
        node?.autofillValue?.takeIf { it.isText }?.textValue?.toString()?.takeIf { it.isNotBlank() }
}
