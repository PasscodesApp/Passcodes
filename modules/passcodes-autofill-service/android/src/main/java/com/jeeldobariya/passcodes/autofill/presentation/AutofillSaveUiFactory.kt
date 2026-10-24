package com.jeeldobariya.passcodes.autofill.presentation

import android.content.Context
import android.os.Build
import android.service.autofill.CharSequenceTransformation
import android.service.autofill.CustomDescription
import android.view.View
import android.view.autofill.AutofillId
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.jeeldobariya.passcodes.autofill.R
import java.util.regex.Pattern

/**
 * Builds the "Save password?" prompt content shown after the user
 * submits a login form (SaveInfo#setCustomDescription).
 *
 * The system keeps its own title and Save / No thanks buttons; this
 * card replaces only the description area. It shows:
 *  - the site or app name, known when the fill response is built;
 *  - the username, copied from the screen at save time through a
 *    [CharSequenceTransformation] (we can't know it any earlier);
 *  - a fixed masked password — the real value is never rendered.
 *
 * Uses explicit colour resources (with night variants), not theme
 * attributes: the system inflates this RemoteViews in its own process.
 */
@RequiresApi(Build.VERSION_CODES.O_MR1)
class AutofillSaveUiFactory(
    private val context: Context
) {

    fun buildDescription(siteLabel: String, usernameId: AutofillId?): CustomDescription {
        val template = RemoteViews(context.packageName, R.layout.autofill_save_description).apply {
            setTextViewText(
                R.id.autofill_save_site,
                siteLabel.ifBlank { context.getString(R.string.autofill_save_unknown_site) }
            )

            // No username field on this form: hide the row altogether.
            if (usernameId == null) {
                setViewVisibility(R.id.autofill_save_username_row, View.GONE)
            }
        }

        val builder = CustomDescription.Builder(template)

        if (usernameId != null) {
            // Copy whatever is in the username field into the card. An
            // empty field doesn't match, so the template's "—" stays.
            builder.addChild(
                R.id.autofill_save_username,
                // The Builder's constructor takes the first (here: only)
                // field; addField() is for adding further ones.
                CharSequenceTransformation.Builder(usernameId, USERNAME_PATTERN, "$1")
                    .build()
            )
        }

        return builder.build()
    }

    private companion object {
        val USERNAME_PATTERN: Pattern = Pattern.compile("^(.+)$")
    }
}
