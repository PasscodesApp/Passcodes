package com.jeeldobariya.passcodes.autofill.unlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillId
import com.jeeldobariya.passcodes.autofill.identity.FieldSpec
import com.jeeldobariya.passcodes.autofill.identity.FieldTraits
import com.jeeldobariya.passcodes.autofill.identity.IdentityFieldKind
import com.jeeldobariya.passcodes.autofill.identity.PlannedField
import com.jeeldobariya.passcodes.autofill.identity.Selection

/**
 * What a locked personal-details suggestion tells the unlock screen.
 *
 * Deliberately contains NO personal data: only which form fields to
 * fill, what each one is (kind + how it accepts values, taken from the
 * form itself), and which of several stored emails/phones to use as a
 * list position. The values are read from the store only after the user
 * has authenticated.
 */
object IdentityUnlockExtras {

    private const val P = "com.jeeldobariya.passcodes.autofill.identity."
    private const val IDS = P + "IDS"
    private const val KINDS = P + "KINDS"
    private const val ACCEPTS_DATE = P + "ACCEPTS_DATE"
    private const val NUMERIC_ONLY = P + "NUMERIC_ONLY"
    private const val MAX_LENGTH = P + "MAX_LENGTH"
    private const val DATE_FORMAT = P + "DATE_FORMAT"
    private const val OPTIONS = P + "OPTIONS"
    private const val EMAIL_INDEX = P + "EMAIL_INDEX"
    private const val PHONE_INDEX = P + "PHONE_INDEX"

    private const val NO_LENGTH = -1

    data class Payload(val fields: List<PlannedField<AutofillId>>, val selection: Selection)

    fun write(intent: Intent, fields: List<PlannedField<AutofillId>>, selection: Selection) {
        intent.putParcelableArrayListExtra(IDS, ArrayList(fields.map { it.id }))
        intent.putExtra(KINDS, fields.map { it.spec.kind.name }.toTypedArray())
        intent.putExtra(ACCEPTS_DATE, fields.map { it.spec.traits.acceptsDate }.toBooleanArray())
        intent.putExtra(NUMERIC_ONLY, fields.map { it.spec.traits.numericOnly }.toBooleanArray())
        intent.putExtra(MAX_LENGTH, fields.map { it.spec.traits.maxLength ?: NO_LENGTH }.toIntArray())
        intent.putExtra(DATE_FORMAT, fields.map { it.spec.traits.dateFormat.orEmpty() }.toTypedArray())

        val options = Bundle()
        fields.forEachIndexed { i, f ->
            f.spec.traits.listOptions?.let { options.putStringArray(i.toString(), it.toTypedArray()) }
        }
        intent.putExtra(OPTIONS, options)

        intent.putExtra(EMAIL_INDEX, selection.emailIndex)
        intent.putExtra(PHONE_INDEX, selection.phoneIndex)
    }

    /** Null if the intent isn't an identity unlock, or is malformed. */
    fun read(intent: Intent): Payload? {
        val ids = readIds(intent) ?: return null
        val kinds = intent.getStringArrayExtra(KINDS) ?: return null
        val acceptsDate = intent.getBooleanArrayExtra(ACCEPTS_DATE) ?: return null
        val numericOnly = intent.getBooleanArrayExtra(NUMERIC_ONLY) ?: return null
        val maxLength = intent.getIntArrayExtra(MAX_LENGTH) ?: return null
        val dateFormat = intent.getStringArrayExtra(DATE_FORMAT) ?: return null
        val options = intent.getBundleExtra(OPTIONS)

        val n = ids.size
        if (n == 0 || listOf(kinds.size, acceptsDate.size, numericOnly.size, maxLength.size, dateFormat.size).any { it != n }) {
            return null
        }

        val fields = ids.indices.map { i ->
            val kind = runCatching { IdentityFieldKind.valueOf(kinds[i]) }.getOrNull() ?: return null
            PlannedField(
                ids[i],
                FieldSpec(
                    kind,
                    FieldTraits(
                        acceptsDate = acceptsDate[i],
                        listOptions = options?.getStringArray(i.toString())?.toList(),
                        numericOnly = numericOnly[i],
                        maxLength = maxLength[i].takeIf { it != NO_LENGTH },
                        dateFormat = dateFormat[i].takeIf { it.isNotEmpty() }
                    )
                )
            )
        }

        return Payload(fields, Selection(intent.getIntExtra(EMAIL_INDEX, 0), intent.getIntExtra(PHONE_INDEX, 0)))
    }

    private fun readIds(intent: Intent): List<AutofillId>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(IDS, AutofillId::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<AutofillId>(IDS)
        }
}
