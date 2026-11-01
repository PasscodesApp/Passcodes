package com.jeeldobariya.passcodes.autofill.identity

import com.jeeldobariya.passcodes.autofill.identity.IdentityFieldKind.*
import com.jeeldobariya.passcodes.autofill.information.AutofillInformation
import java.time.LocalDate
import java.time.ZoneId

/** How the form field accepts a value (independent of what it asks for). */
data class FieldTraits(
    /** The view is a date picker (AUTOFILL_TYPE_DATE). */
    val acceptsDate: Boolean = false,
    /** The view is a list / spinner; values must be one of these options. */
    val listOptions: List<String>? = null,
    /** Numeric keyboard: digits only. */
    val numericOnly: Boolean = false,
    /** Known maximum length (HTML maxlength). Longer values are skipped, never truncated. */
    val maxLength: Int? = null,
    /** Pattern for a plain-text date field, from [DateFormats.infer]. */
    val dateFormat: String? = null
)

data class FieldSpec(val kind: IdentityFieldKind, val traits: FieldTraits = FieldTraits())

/** Which of several emails / phones a dataset uses (0 = default). */
data class Selection(val emailIndex: Int = 0, val phoneIndex: Int = 0)

sealed interface FillValue {
    data class Text(val value: String) : FillValue
    data class ListIndex(val index: Int) : FillValue
    data class DateMillis(val millis: Long) : FillValue
}

/**
 * Turns the stored information into the exact value one field needs, or
 * null when it can't be done *safely* — wrong format, doesn't fit,
 * option not in the list. Skipping beats a wrong or truncated value.
 *
 * The same function runs for unlocked datasets (in the service) and
 * after authentication (in the unlock screen), so both fill identically.
 */
object IdentityFiller {

    fun resolve(
        info: AutofillInformation,
        selection: Selection,
        spec: FieldSpec,
        zone: ZoneId = ZoneId.systemDefault()
    ): FillValue? {
        val traits = spec.traits
        return when (spec.kind) {
            BIRTH_DATE -> resolveBirthDate(info.birthDate, traits, zone)

            COUNTRY -> listOrText(info.address.country, traits)
            REGION -> listOrText(info.address.region, traits)

            PHONE_COUNTRY_CODE -> {
                val cc = info.orderedPhones().getOrNull(selection.phoneIndex)?.countryCode.orEmpty()
                if (cc.isEmpty()) return null
                val options = traits.listOptions
                if (options != null) {
                    val matches = options.indices.filter { Regex("""(^|\D)\+?$cc(\D|$)""").containsMatchIn(options[it]) }
                    return if (matches.size == 1) FillValue.ListIndex(matches.first()) else null
                }
                val plain = if (traits.numericOnly || traits.maxLength == cc.length) cc else "+$cc"
                text(plain, traits)
            }

            else -> text(textFor(info, selection, spec.kind, traits), traits)
        }
    }

    private fun textFor(info: AutofillInformation, sel: Selection, kind: IdentityFieldKind, traits: FieldTraits): String? =
        when (kind) {
            FULL_NAME -> info.effectiveFullName()
            FIRST_NAME -> info.firstName
            LAST_NAME -> info.lastName
            EMAIL -> info.orderedEmails().getOrNull(sel.emailIndex)?.address
            PHONE_NATIONAL -> info.orderedPhones().getOrNull(sel.phoneIndex)?.number
            PHONE_FULL -> info.orderedPhones().getOrNull(sel.phoneIndex)?.let { phone ->
                // International form drops a single leading trunk "0":
                // "098765 43210" in India is "+91 98765 43210".
                val national = if (phone.countryCode.isNotEmpty()) phone.number.removePrefix("0") else phone.number
                when {
                    phone.countryCode.isEmpty() -> phone.number
                    traits.numericOnly -> phone.countryCode + national
                    else -> "+" + phone.countryCode + national
                }
            }
            ADDRESS_FULL -> info.effectiveFullAddress()
            STREET -> info.address.street
            STREET2 -> info.address.street2
            CITY -> info.address.city
            POSTAL_CODE -> info.address.postalCode
            else -> null
        }

    private fun text(value: String?, traits: FieldTraits): FillValue? {
        val v = value?.takeIf { it.isNotBlank() } ?: return null
        if (traits.listOptions != null) return null // a list can't take free text
        if (traits.numericOnly && !v.all(Char::isDigit)) return null
        val limit = traits.maxLength
        if (limit != null && v.length > limit) return null
        return FillValue.Text(v)
    }

    private fun listOrText(value: String, traits: FieldTraits): FillValue? {
        val v = value.takeIf { it.isNotBlank() } ?: return null
        val options = traits.listOptions ?: return text(v, traits)
        val wanted = normalise(v)
        val matches = options.indices.filter { normalise(options[it]) == wanted }
        return if (matches.size == 1) FillValue.ListIndex(matches.first()) else null
    }

    private fun resolveBirthDate(iso: String, traits: FieldTraits, zone: ZoneId): FillValue? {
        val m = Regex("""^(\d{4})-(\d{2})-(\d{2})$""").matchEntire(iso) ?: return null
        val (y, mo, d) = m.destructured
        if (traits.acceptsDate) {
            val date = runCatching { LocalDate.of(y.toInt(), mo.toInt(), d.toInt()) }.getOrNull() ?: return null
            // Midnight in the device zone: that's how the platform's date
            // pickers read the value back.
            return FillValue.DateMillis(date.atStartOfDay(zone).toInstant().toEpochMilli())
        }
        val pattern = traits.dateFormat ?: return null
        return text(DateFormats.format(iso, pattern), traits)
    }

    private fun normalise(s: String) = s.lowercase().filter { it.isLetterOrDigit() }
}
