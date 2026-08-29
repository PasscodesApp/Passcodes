package com.jeeldobariya.passcodes.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec

import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi

class AutofillPresentationFactory(
    private val context: Context
) {

    fun applyValue(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        value: AutofillValue,
        entry: PasscodesDatabase.PasswordEntry,
        inlineSpec: InlinePresentationSpec?
    ) {
        val menuPresentation = buildMenuPresentation(entry)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                applyWithPresentations(
                    datasetBuilder = datasetBuilder,
                    id = id,
                    value = value,
                    entry = entry,
                    menuPresentation = menuPresentation,
                    inlineSpec = inlineSpec
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                inlineSpec != null -> {

                applyWithInline(
                    datasetBuilder = datasetBuilder,
                    id = id,
                    value = value,
                    entry = entry,
                    menuPresentation = menuPresentation,
                    inlineSpec = inlineSpec
                )
            }

            else -> {
                datasetBuilder.setValue(
                    id,
                    value,
                    menuPresentation
                )
            }
        }
    }

    private fun buildMenuPresentation(
        entry: PasscodesDatabase.PasswordEntry
    ): RemoteViews {
        return RemoteViews(
            context.packageName,
            R.layout.autofill_item
        ).apply {
            setImageViewResource(
                R.id.autofill_icon,
                R.drawable.ic_autofill_lock
            )

            setTextViewText(
                R.id.autofill_domain,
                entry.domain
            )

            setTextViewText(
                R.id.autofill_username,
                entry.username
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun applyWithPresentations(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        value: AutofillValue,
        entry: PasscodesDatabase.PasswordEntry,
        menuPresentation: RemoteViews,
        inlineSpec: InlinePresentationSpec?
    ) {
        val presentationsBuilder = Presentations.Builder()
            .setMenuPresentation(menuPresentation)
            .setDialogPresentation(menuPresentation)

        inlineSpec?.let { spec ->
            buildInlinePresentation(spec, entry)?.let {
                presentationsBuilder.setInlinePresentation(it)
            }
        }

        val field = Field.Builder()
            .setValue(value)
            .setPresentations(
                presentationsBuilder.build()
            )
            .build()

        datasetBuilder.setField(
            id,
            field
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun applyWithInline(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        value: AutofillValue,
        entry: PasscodesDatabase.PasswordEntry,
        menuPresentation: RemoteViews,
        inlineSpec: InlinePresentationSpec
    ) {
        val inlinePresentation =
            buildInlinePresentation(
                inlineSpec,
                entry
            )

        if (inlinePresentation != null) {
            datasetBuilder.setValue(
                id,
                value,
                menuPresentation,
                inlinePresentation
            )
        } else {
            datasetBuilder.setValue(
                id,
                value,
                menuPresentation
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildInlinePresentation(
        spec: InlinePresentationSpec,
        entry: PasscodesDatabase.PasswordEntry
    ): InlinePresentation? {

        if (
            !UiVersions
                .getVersions(spec.style)
                .contains(
                    UiVersions.INLINE_UI_VERSION_1
                )
        ) {
            return null
        }

        val icon = Icon.createWithResource(
            context,
            R.drawable.ic_autofill_lock
        )

        val content =
            InlineSuggestionUi
                .newContentBuilder(createInlineSuggestionIntent(entry))
                .setTitle(entry.username)
                .setSubtitle(entry.domain)
                .setStartIcon(icon)
                .build()

        return InlinePresentation(
            content.slice,
            spec,
            false
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createInlineSuggestionIntent(
        entry: PasscodesDatabase.PasswordEntry
    ): PendingIntent {

        val intent = Intent(
            context,
            AutofillSettingsActivity::class.java
        ).apply {
            action = "com.jeeldobariya.passcodes.AUTOFILL_SUGGESTION"
        }

        return PendingIntent.getActivity(
            context,
            entry.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }
}
