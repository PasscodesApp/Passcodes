package com.jeeldobariya.passcodes.autofill.information

/**
 * The personal details a user keeps in Passcodes purely for Autofill:
 * names, several emails and phone numbers, a birth date and one postal
 * address. Every field is optional.
 *
 * Pure Kotlin on purpose (no Android types) so the validation rules can
 * be unit-tested off-device.
 *
 * Privacy: none of these classes ever prints its contents —
 * `toString()` is redacted so an accidental log line cannot leak data.
 */
data class EmailEntry(
    val label: String = "",
    val address: String = "",
    val isDefault: Boolean = false
) {
    override fun toString() = "EmailEntry(<redacted>)"
}

data class PhoneEntry(
    val label: String = "",
    /** Dialing code digits only, without "+": "91". May be empty. */
    val countryCode: String = "",
    /** National number, digits only, as the user entered it (no country code). */
    val number: String = "",
    val isDefault: Boolean = false
) {
    override fun toString() = "PhoneEntry(<redacted>)"
}

data class AddressInfo(
    val street: String = "",
    /** Apartment / suite / unit / second line. */
    val street2: String = "",
    val city: String = "",
    val region: String = "",
    val postalCode: String = "",
    val country: String = "",
    /** Optional one-line version; when blank it is composed from the parts. */
    val full: String = ""
) {
    val isEmpty: Boolean
        get() = listOf(street, street2, city, region, postalCode, country, full).all { it.isBlank() }

    override fun toString() = "AddressInfo(<redacted>)"
}

data class AutofillInformation(
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    /** ISO date "yyyy-MM-dd", or empty. */
    val birthDate: String = "",
    val emails: List<EmailEntry> = emptyList(),
    val phones: List<PhoneEntry> = emptyList(),
    val address: AddressInfo = AddressInfo()
) {

    val isEmpty: Boolean
        get() = firstName.isBlank() && lastName.isBlank() && fullName.isBlank() &&
            birthDate.isBlank() && emails.isEmpty() && phones.isEmpty() && address.isEmpty

    /** The user's full name, or "first last" if only the parts were given. */
    fun effectiveFullName(): String =
        fullName.ifBlank { listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ") }

    /** Default first, then the user's own order. */
    fun orderedEmails(): List<EmailEntry> = emails.sortedByDescending { it.isDefault }

    fun orderedPhones(): List<PhoneEntry> = phones.sortedByDescending { it.isDefault }

    /** One-line address: the explicit one, else the parts joined with ", ". */
    fun effectiveFullAddress(): String = address.full.ifBlank {
        listOf(address.street, address.street2, address.city, address.region, address.postalCode, address.country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    /** Applies every validation/normalisation rule; safe to call repeatedly. */
    fun sanitized(): AutofillInformation {
        val cleanEmails = emails
            .mapNotNull { e ->
                val address = cleanText(e.address, MAX_EMAIL).takeIf(::isPlausibleEmail) ?: return@mapNotNull null
                EmailEntry(cleanText(e.label, MAX_LABEL), address, e.isDefault)
            }
            .distinctBy { it.address.lowercase() }
            .take(MAX_EMAILS)

        val cleanPhones = phones
            .mapNotNull { p ->
                val cc = p.countryCode.filter(Char::isDigit).take(MAX_COUNTRY_CODE)
                val number = p.number.filter(Char::isDigit)
                if (number.length !in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS) return@mapNotNull null
                PhoneEntry(cleanText(p.label, MAX_LABEL), cc, number, p.isDefault)
            }
            .distinctBy { it.countryCode + "|" + it.number }
            .take(MAX_PHONES)

        return AutofillInformation(
            firstName = cleanText(firstName, MAX_NAME),
            lastName = cleanText(lastName, MAX_NAME),
            fullName = cleanText(fullName, MAX_NAME * 2),
            birthDate = birthDate.trim().takeIf(::isValidIsoDate).orEmpty(),
            // Exactly one default per list: the first flagged one, else the first entry.
            emails = cleanEmails.mapIndexed { i, e -> e.copy(isDefault = i == defaultIndexOf(cleanEmails) { it.isDefault }) },
            phones = cleanPhones.mapIndexed { i, p -> p.copy(isDefault = i == defaultIndexOf(cleanPhones) { it.isDefault }) },
            address = AddressInfo(
                street = cleanText(address.street, MAX_ADDRESS_LINE),
                street2 = cleanText(address.street2, MAX_ADDRESS_LINE),
                city = cleanText(address.city, MAX_NAME),
                region = cleanText(address.region, MAX_NAME),
                postalCode = cleanText(address.postalCode, MAX_POSTAL),
                country = cleanText(address.country, MAX_NAME),
                full = cleanText(address.full, MAX_ADDRESS_LINE * 2)
            )
        )
    }

    /** Plain maps/lists/strings/booleans only — safe to hand to JS or JSON. */
    fun toMap(): Map<String, Any?> = mapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "fullName" to fullName,
        "birthDate" to birthDate,
        "emails" to emails.map { mapOf("label" to it.label, "address" to it.address, "isDefault" to it.isDefault) },
        "phones" to phones.map {
            mapOf("label" to it.label, "countryCode" to it.countryCode, "number" to it.number, "isDefault" to it.isDefault)
        },
        "address" to mapOf(
            "street" to address.street,
            "street2" to address.street2,
            "city" to address.city,
            "region" to address.region,
            "postalCode" to address.postalCode,
            "country" to address.country,
            "full" to address.full
        )
    )

    override fun toString() = "AutofillInformation(<redacted>)"

    companion object {
        private val ISO_DATE = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")
        const val MAX_EMAILS = 5
        const val MAX_PHONES = 5

        private const val MAX_NAME = 100
        private const val MAX_LABEL = 40
        private const val MAX_EMAIL = 254
        private const val MAX_ADDRESS_LINE = 200
        private const val MAX_POSTAL = 20
        private const val MAX_COUNTRY_CODE = 4
        private const val MIN_PHONE_DIGITS = 4
        private const val MAX_PHONE_DIGITS = 15

        /**
         * Reads the map shape produced by [toMap] (or sent from React
         * Native / parsed from JSON). Unknown or mistyped entries are
         * ignored, and the result is always [sanitized].
         */
        fun fromMap(raw: Map<*, *>?): AutofillInformation {
            if (raw == null) return AutofillInformation()

            fun str(map: Map<*, *>?, key: String) = (map?.get(key) as? String).orEmpty()

            val emails = (raw["emails"] as? List<*>).orEmpty().mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                EmailEntry(str(m, "label"), str(m, "address"), m["isDefault"] as? Boolean ?: false)
            }
            val phones = (raw["phones"] as? List<*>).orEmpty().mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                PhoneEntry(str(m, "label"), str(m, "countryCode"), str(m, "number"), m["isDefault"] as? Boolean ?: false)
            }
            val a = raw["address"] as? Map<*, *>

            return AutofillInformation(
                firstName = str(raw, "firstName"),
                lastName = str(raw, "lastName"),
                fullName = str(raw, "fullName"),
                birthDate = str(raw, "birthDate"),
                emails = emails,
                phones = phones,
                address = AddressInfo(
                    street = str(a, "street"),
                    street2 = str(a, "street2"),
                    city = str(a, "city"),
                    region = str(a, "region"),
                    postalCode = str(a, "postalCode"),
                    country = str(a, "country"),
                    full = str(a, "full")
                )
            ).sanitized()
        }

        /** Trims, turns control characters (newlines, tabs…) into spaces, caps the length. */
        internal fun cleanText(value: String, max: Int): String =
            value.map { if (it.code < 0x20 || it.code == 0x7f) ' ' else it }
                .joinToString("")
                .trim()
                .take(max)

        internal fun isPlausibleEmail(value: String): Boolean {
            if (value.isEmpty() || value.any { it.isWhitespace() }) return false
            if (value.count { it == '@' } != 1) return false
            val (local, domain) = value.split('@')
            return local.isNotEmpty() && domain.contains('.') &&
                !domain.startsWith('.') && !domain.endsWith('.') && !domain.contains("..")
        }

        internal fun isValidIsoDate(value: String): Boolean {
            val m = ISO_DATE.matchEntire(value) ?: return false
            val (y, mo, d) = m.destructured.toList().map(String::toInt)
            if (y !in 1900..2100 || mo !in 1..12) return false
            val leap = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
            val days = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)[mo - 1]
            return d in 1..days
        }

        private inline fun <T> defaultIndexOf(list: List<T>, isDefault: (T) -> Boolean): Int =
            list.indexOfFirst(isDefault).coerceAtLeast(0)
    }
}
