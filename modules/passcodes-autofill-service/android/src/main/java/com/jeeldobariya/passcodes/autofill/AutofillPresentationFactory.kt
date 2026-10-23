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

/**
 * Builds the on-screen presentation for a single Autofill dataset value.
 *
 * Three tiers, each isolated behind its own @RequiresApi check so newer
 * API types are never referenced on an OS version that doesn't have them:
 *  - the classic dropdown, a RemoteViews — API 26+.
 *  - inline suggestions in the keyboard strip — API 30+.
 *  - the API 34+ Presentations bundle (menu + inline + dialog together).
 */
class AutofillPresentationFactory(
    private val context: Context
) {

    /** Applies a real, fillable value with the full tiered presentation. */
    fun applyValue(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        value: AutofillValue,
        entry: PasscodesDatabase.PasswordEntry,
        inlineSpec: InlinePresentationSpec?
    ) {
        val menuPresentation = buildMenuPresentation(entry, locked = false)

        when {
            // Presentations/Field/Dataset.Builder#setField were added in
            // API 34 (UPSIDE_DOWN_CAKE), NOT API 33 (TIRAMISU) — using
            // the wrong constant here throws NoSuchMethodError on real
            // Android 13 devices even though it compiles fine.
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
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

    /**
     * Applies a "locked" placeholder for a field: no real value is
     * attached (null), only a presentation showing the credential is
     * available but requires unlocking first. Used together with
     * Dataset.Builder#setAuthentication by PasscodesAutofillService
     * when the biometric lock is active.
     *
     * Deliberately dropdown-only (no inline suggestion tier) — an
     * inline chip that still needs a follow-up unlock step is a more
     * confusing tap target than a clear "tap to unlock" row in the
     * dropdown, so locked entries skip inline entirely.
     */
    fun applyLockedPlaceholder(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        entry: PasscodesDatabase.PasswordEntry
    ) {
        datasetBuilder.setValue(
            id,
            null,
            buildMenuPresentation(entry, locked = true)
        )
    }

    private fun buildMenuPresentation(
        entry: PasscodesDatabase.PasswordEntry,
        locked: Boolean
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
                if (locked) context.getString(R.string.autofill_tap_to_unlock) else entry.username
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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
