package com.jeeldobariya.passcodes.autofill.presentation

import com.jeeldobariya.passcodes.autofill.R
import com.jeeldobariya.passcodes.autofill.settings.AutofillSettingsActivity
import com.jeeldobariya.passcodes.autofill.identity.PlanKind
import com.jeeldobariya.passcodes.database.model.PasswordEntry
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

    // -- What each kind of suggestion looks like --------------------------

    fun displayForCredential(entry: PasswordEntry, locked: Boolean, requestKey: Int): DatasetDisplay {
        val tapToUnlock = context.getString(R.string.autofill_tap_to_unlock)
        return if (locked) {
            DatasetDisplay(
                title = entry.domain, subtitle = tapToUnlock,
                inlineTitle = entry.domain, inlineSubtitle = tapToUnlock,
                iconRes = R.drawable.ic_autofill_lock, requestKey = requestKey
            )
        } else {
            DatasetDisplay(
                title = entry.domain, subtitle = entry.username,
                inlineTitle = entry.username.ifBlank { entry.domain }, inlineSubtitle = entry.domain,
                iconRes = R.drawable.ic_autofill_key, requestKey = requestKey
            )
        }
    }

    /** [summary] is only shown when unlocked; a locked suggestion shows "Tap to unlock" instead. */
    fun displayForIdentity(kind: PlanKind, summary: String, locked: Boolean, requestKey: Int): DatasetDisplay {
        val title = context.getString(
            when (kind) {
                PlanKind.CONTACT_BUNDLE -> R.string.autofill_identity_contact
                PlanKind.EMAIL_ALTERNATIVE -> R.string.autofill_identity_email_alternative
                PlanKind.PHONE_ALTERNATIVE -> R.string.autofill_identity_phone_alternative
                PlanKind.BIRTH_DATE -> R.string.autofill_identity_birth_date
                PlanKind.ADDRESS -> R.string.autofill_identity_address
            }
        )
        val icon = when (kind) {
            PlanKind.BIRTH_DATE -> R.drawable.ic_autofill_event
            PlanKind.ADDRESS -> R.drawable.ic_autofill_location
            else -> R.drawable.ic_autofill_person
        }
        return if (locked) {
            val tapToUnlock = context.getString(R.string.autofill_tap_to_unlock)
            DatasetDisplay(
                title = title, subtitle = tapToUnlock,
                inlineTitle = title, inlineSubtitle = tapToUnlock,
                iconRes = R.drawable.ic_autofill_lock, requestKey = requestKey
            )
        } else {
            DatasetDisplay(
                title = title, subtitle = summary,
                inlineTitle = summary.ifBlank { title }, inlineSubtitle = title,
                iconRes = icon, requestKey = requestKey
            )
        }
    }

    /** Applies a real, fillable value with the full tiered presentation. */
    fun applyValue(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        value: AutofillValue,
        display: DatasetDisplay,
        inlineSpec: InlinePresentationSpec?
    ) {
        val menuPresentation = buildMenuPresentation(display)

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
                    display = display,
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
                    display = display,
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
     * If [inlineSpec] is supplied (the user kept inline suggestions on),
     * the locked row is also offered as a keyboard chip. It shows the
     * site name and "Tap to unlock" only — the username stays hidden
     * until authentication succeeds, which is why the settings screen
     * recommends turning inline off when the lock is on.
     */
    fun applyLockedPlaceholder(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        display: DatasetDisplay,
        inlineSpec: InlinePresentationSpec?
    ) {
        val menuPresentation = buildMenuPresentation(display)

        val inlinePresentation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineSpec != null) {
                buildInlinePresentation(inlineSpec, display)
            } else {
                null
            }

        if (inlinePresentation != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            datasetBuilder.setValue(id, null, menuPresentation, inlinePresentation)
        } else {
            datasetBuilder.setValue(id, null, menuPresentation)
        }
    }

    private fun buildMenuPresentation(display: DatasetDisplay): RemoteViews {
        return RemoteViews(
            context.packageName,
            R.layout.autofill_item
        ).apply {
            setImageViewResource(R.id.autofill_icon, display.iconRes)
            setTextViewText(R.id.autofill_domain, display.title)
            setTextViewText(R.id.autofill_username, display.subtitle)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun applyWithPresentations(
        datasetBuilder: Dataset.Builder,
        id: AutofillId,
        value: AutofillValue,
        display: DatasetDisplay,
        menuPresentation: RemoteViews,
        inlineSpec: InlinePresentationSpec?
    ) {
        val presentationsBuilder = Presentations.Builder()
            .setMenuPresentation(menuPresentation)
            .setDialogPresentation(menuPresentation)

        inlineSpec?.let { spec ->
            buildInlinePresentation(spec, display)?.let {
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
        display: DatasetDisplay,
        menuPresentation: RemoteViews,
        inlineSpec: InlinePresentationSpec
    ) {
        val inlinePresentation =
            buildInlinePresentation(
                inlineSpec,
                display
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
        display: DatasetDisplay
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

        // The vector drawables are plain black shapes meant to be tinted;
        // untinted they'd be invisible on a dark keyboard strip. The tint
        // colour has a night variant, so it follows the system theme.
        val icon = Icon.createWithResource(context, display.iconRes)
            .setTint(context.getColor(R.color.autofill_inline_icon))

        val content =
            InlineSuggestionUi
                .newContentBuilder(createInlineSuggestionIntent(display))
                .setTitle(display.inlineTitle)
                .setSubtitle(display.inlineSubtitle)
                .setStartIcon(icon)
                .setContentDescription("${display.inlineTitle}, ${display.inlineSubtitle}")
                .build()

        return InlinePresentation(
            content.slice,
            spec,
            false
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createInlineSuggestionIntent(
        display: DatasetDisplay
    ): PendingIntent {

        val intent = Intent(
            context,
            AutofillSettingsActivity::class.java
        ).apply {
            action = "com.jeeldobariya.passcodes.AUTOFILL_SUGGESTION"
        }

        return PendingIntent.getActivity(
            context,
            display.requestKey,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }
}
