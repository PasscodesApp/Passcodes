package com.jeeldobariya.passcodes.autofill.identity

import com.jeeldobariya.passcodes.autofill.identity.IdentityFieldKind.*

/**
 * Decides what kind of personal detail (if any) one form field asks for.
 *
 * Conservative by design: it is better to offer nothing than to put a
 * detail into the wrong field. Evidence is weighed, in order of
 * reliability:
 *
 *   explicit autofill hint (100) > HTML autocomplete (90) > HTML type (80)
 *   > Android input type (60) > keywords in ids / labels (45 each)
 *
 * Agreeing evidence from several sources adds up (+10 each), which is
 * how keyword-only fields (no hints, as in many apps) can still reach
 * the higher bar the address and birth-date kinds require.
 *
 * Pure Kotlin — no Android classes — so it is unit-testable off-device.
 */
object IdentityFieldClassifier {

    private enum class Source { HINT, AUTOCOMPLETE, HTML_TYPE, INPUT_KIND, ID_KEYWORD, LABEL_KEYWORD }

    private data class Vote(
        val kind: IdentityFieldKind,
        val weight: Int,
        val source: Source,
        val contextKinds: Set<IdentityFieldKind> = emptySet()
    )

    fun classify(signals: NodeSignals): Classification? {
        if (signals.inputKind == InputKind.PASSWORD) return null
        if (signals.htmlType == "password" || signals.htmlType == "search") return null

        val votes = mutableListOf<Vote>()

        // 1. Explicit Android autofill hints.
        for (hint in signals.hints) {
            when (val r = lookupToken(hint)) {
                is TokenResult.Kind -> votes += Vote(r.kind, 100, Source.HINT)
                TokenResult.Excluded -> return null
                TokenResult.Unknown -> Unit
            }
        }

        // 2. HTML autocomplete (the last token wins: "shipping given-name").
        signals.htmlAutocomplete
            ?.trim()?.lowercase()?.split(WHITESPACE)?.lastOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?.let { token ->
                when (val r = lookupToken(token)) {
                    is TokenResult.Kind -> votes += Vote(r.kind, 90, Source.AUTOCOMPLETE)
                    TokenResult.Excluded -> return null
                    TokenResult.Unknown -> Unit
                }
            }

        // 3. HTML input type.
        when (signals.htmlType) {
            "email" -> votes += Vote(EMAIL, 80, Source.HTML_TYPE)
            "tel" -> votes += Vote(PHONE_FULL, 80, Source.HTML_TYPE)
        }

        // 4. Keywords in identifiers and in visible text.
        val idMatch = keyword(tokenize(signals.idTexts))
        val labelMatch = keyword(tokenize(signals.labelTexts))
        if (idMatch === Keyword.Reject || labelMatch === Keyword.Reject) return null
        if (idMatch is Keyword.Match) {
            votes += Vote(idMatch.kind, idMatch.weight, Source.ID_KEYWORD, idMatch.contextKinds)
        }
        if (labelMatch is Keyword.Match) {
            votes += Vote(labelMatch.kind, labelMatch.weight, Source.LABEL_KEYWORD, labelMatch.contextKinds)
        }

        // 5. Android input type: confirms a keyword match, or stands alone
        //    only where the type alone is meaningful (email, phone).
        val keywordKinds = votes.filter { it.source == Source.ID_KEYWORD || it.source == Source.LABEL_KEYWORD }
            .map { it.kind }.toSet()
        when (signals.inputKind) {
            InputKind.EMAIL ->
                if (keywordKinds.isEmpty() || EMAIL in keywordKinds) votes += Vote(EMAIL, 60, Source.INPUT_KIND)
            InputKind.PHONE -> when {
                keywordKinds.isEmpty() -> votes += Vote(PHONE_FULL, 60, Source.INPUT_KIND)
                else -> keywordKinds.filter { it.isPhone }.forEach { votes += Vote(it, 60, Source.INPUT_KIND) }
            }
            InputKind.PERSON_NAME ->
                keywordKinds.filter { it == FULL_NAME || it == FIRST_NAME || it == LAST_NAME }
                    .forEach { votes += Vote(it, 60, Source.INPUT_KIND) }
            InputKind.POSTAL_ADDRESS ->
                keywordKinds.filter { it.partition == IdentityPartition.ADDRESS }
                    .forEach { votes += Vote(it, 60, Source.INPUT_KIND) }
            InputKind.DATETIME ->
                if (BIRTH_DATE in keywordKinds) votes += Vote(BIRTH_DATE, 60, Source.INPUT_KIND)
            InputKind.PLAIN, InputKind.NUMBER, InputKind.PASSWORD -> Unit
        }

        if (votes.isEmpty()) return null

        // Combine votes per kind: strongest source + 10 per extra agreeing source.
        val scored = votes.groupBy { it.kind }.map { (kind, v) ->
            val confidence = (v.maxOf { it.weight } + 10 * (v.map { it.source }.distinct().size - 1)).coerceAtMost(100)
            Triple(kind, confidence, v)
        }.sortedByDescending { it.second }

        val top = scored.first()
        val runnerUp = scored.getOrNull(1)
        // Two different, fairly strong readings of the same field: don't guess.
        if (runnerUp != null && runnerUp.second >= 55 && top.second - runnerUp.second < 30) return null

        val topVotes = top.third
        return Classification(
            kind = top.first,
            confidence = top.second,
            explicit = topVotes.any { it.source == Source.HINT || it.source == Source.AUTOCOMPLETE },
            contextKinds = topVotes.flatMap { it.contextKinds }.toSet()
        )
    }

    // -----------------------------------------------------------------
    // Hint / autocomplete tokens
    // -----------------------------------------------------------------

    private sealed interface TokenResult {
        data class Kind(val kind: IdentityFieldKind) : TokenResult
        /** A real hint for something we deliberately don't fill (credentials, OTP, cards, middle name…). */
        data object Excluded : TokenResult
        data object Unknown : TokenResult
    }

    private val TOKEN_KINDS: Map<String, IdentityFieldKind> = mapOf(
        "emailaddress" to EMAIL, "email" to EMAIL,
        "phone" to PHONE_FULL, "phonenumber" to PHONE_FULL, "tel" to PHONE_FULL,
        "phonenational" to PHONE_NATIONAL, "telnational" to PHONE_NATIONAL, "phonenumbernational" to PHONE_NATIONAL,
        "phonecountrycode" to PHONE_COUNTRY_CODE, "telcountrycode" to PHONE_COUNTRY_CODE,
        "name" to FULL_NAME, "personname" to FULL_NAME, "fullname" to FULL_NAME,
        "persongivenname" to FIRST_NAME, "personnamegiven" to FIRST_NAME, "givenname" to FIRST_NAME, "firstname" to FIRST_NAME,
        "personfamilyname" to LAST_NAME, "personnamefamily" to LAST_NAME, "familyname" to LAST_NAME,
        "lastname" to LAST_NAME, "surname" to LAST_NAME,
        "birthdatefull" to BIRTH_DATE, "bday" to BIRTH_DATE, "birthdate" to BIRTH_DATE,
        "postaladdress" to ADDRESS_FULL,
        "streetaddress" to STREET, "postaladdressstreetaddress" to STREET, "addressline1" to STREET,
        "extendedaddress" to STREET2, "postaladdressextendedaddress" to STREET2, "addressline2" to STREET2,
        "addresslocality" to CITY, "postaladdresslocality" to CITY, "addresslevel2" to CITY,
        "addressregion" to REGION, "postaladdressregion" to REGION, "addresslevel1" to REGION,
        "postalcode" to POSTAL_CODE, "postaladdresspostalcode" to POSTAL_CODE,
        "addresscountry" to COUNTRY, "postaladdresscountry" to COUNTRY, "country" to COUNTRY, "countryname" to COUNTRY
    )

    private val EXCLUDED_TOKENS = setOf(
        "personmiddlename", "personnamemiddle", "additionalname", "personnamemiddleinitial",
        "gender", "sex", "birthdateday", "birthdatemonth", "birthdateyear", "bdayday", "bdaymonth", "bdayyear",
        "nameprefix", "namesuffix", "personnameprefix", "personnamesuffix", "honorificprefix", "honorificsuffix",
        "nickname", "organization", "organizationtitle", "jobtitle", "off"
    )

    /** HTML autocomplete "cc-*" tokens, normalised. */
    private val CARD_AUTOCOMPLETE = setOf(
        "ccname", "ccgivenname", "ccadditionalname", "ccfamilyname", "ccnumber",
        "ccexp", "ccexpmonth", "ccexpyear", "cccsc", "cctype"
    )

    private fun lookupToken(raw: String): TokenResult {
        val n = raw.lowercase().filter { it.isLetterOrDigit() }
        if (n.isEmpty()) return TokenResult.Unknown
        if (n in EXCLUDED_TOKENS) return TokenResult.Excluded
        if ("password" in n || "username" in n || "otp" in n || "creditcard" in n ||
            "onetimecode" in n || n in CARD_AUTOCOMPLETE
        ) return TokenResult.Excluded
        return TOKEN_KINDS[n]?.let { TokenResult.Kind(it) } ?: TokenResult.Unknown
    }

    // -----------------------------------------------------------------
    // Keywords in ids and visible text
    // -----------------------------------------------------------------

    private sealed interface Keyword {
        data object None : Keyword
        /** The text says this field is something we must never touch (search, OTP, card, ID numbers…). */
        data object Reject : Keyword
        data class Match(
            val kind: IdentityFieldKind,
            val weight: Int = 45,
            val contextKinds: Set<IdentityFieldKind> = emptySet()
        ) : Keyword
    }

    private val GLOBAL_NEGATIVE = setOf(
        "search", "query", "find", "otp", "cvv", "cvc", "cvn", "pin", "card", "iban", "swift", "routing", "ifsc",
        "promo", "coupon", "voucher", "captcha", "token", "secret", "passcode", "password", "passwd",
        // Government-ID style fields are out of scope; never mistake them for anything else.
        "passport", "aadhaar", "aadhar", "nric", "ssn", "tax", "licence", "license", "citizen"
    )

    private val NAME_NEGATIVE = setOf(
        "user", "username", "company", "business", "organization", "organisation", "org", "file", "display",
        "nick", "nickname", "account", "product", "brand", "team", "school", "mother", "father", "spouse",
        "guardian", "parent", "emergency", "referrer", "referral", "friend", "beneficiary", "nominee",
        "relative", "reference", "holder", "bank", "street", "city", "hotel", "airline", "flight", "domain",
        "host", "middle", "maiden", "previous", "other"
    )

    private val ADDRESS_NEGATIVE = setOf(
        "ip", "web", "url", "site", "website", "mac", "wallet", "crypto", "email", "mail", "domain"
    )

    private val CONTACT_CONTEXT = setOf(EMAIL, PHONE_FULL, PHONE_NATIONAL, FIRST_NAME, LAST_NAME)

    // Compiled once: tokenize() runs for every field of every form.
    private val WHITESPACE = Regex("\\s+")
    private val NON_ALNUM = Regex("[^a-z0-9]+")
    private val CAMEL_BOUNDARY = Regex("([a-z])([A-Z])")
    private val LETTER_DIGIT_BOUNDARY = Regex("([A-Za-z])([0-9])")
    private val DIGIT_LETTER_BOUNDARY = Regex("([0-9])([A-Za-z])")

    internal fun tokenize(texts: List<String>): Set<String> {
        val out = mutableSetOf<String>()
        for (text in texts) {
            val spaced = text
                .replace(CAMEL_BOUNDARY, "$1 $2")
                .replace(LETTER_DIGIT_BOUNDARY, "$1 $2")
                .replace(DIGIT_LETTER_BOUNDARY, "$1 $2")
            spaced.lowercase().split(NON_ALNUM).filterTo(out) { it.isNotEmpty() }
        }
        return out
    }

    private fun keyword(t: Set<String>): Keyword {
        if (t.isEmpty()) return Keyword.None
        if (t.any { it in GLOBAL_NEGATIVE }) return Keyword.Reject

        fun has(vararg w: String) = w.all { it in t }
        fun any(vararg w: String) = w.any { it in t }

        // Email
        if (any("email", "emailaddress", "mail")) {
            return if (any("subject", "recipient", "newsletter", "cc", "bcc")) Keyword.None else Keyword.Match(EMAIL)
        }

        // Phone family
        if (any("fax", "extension", "ext")) return Keyword.None
        val phoneish = any("phone", "telephone", "tel", "mobile", "cell", "cellphone", "mobilenumber",
            "phonenumber", "msisdn", "whatsapp", "contactnumber") || has("contact", "number")
        val countryCode = has("country", "code") || any("countrycode", "dialcode", "isd") || (phoneish && "cc" in t)
        if (countryCode) {
            return if (phoneish || any("dialcode", "isd")) {
                Keyword.Match(PHONE_COUNTRY_CODE)
            } else {
                // Bare "Country code": only trusted next to a phone field.
                Keyword.Match(PHONE_COUNTRY_CODE, weight = 35, contextKinds = setOf(PHONE_FULL, PHONE_NATIONAL))
            }
        }
        if (phoneish) return Keyword.Match(if ("national" in t) PHONE_NATIONAL else PHONE_FULL)

        // Birth date
        if (any("dob", "birthdate", "birthday", "bday", "dateofbirth") || "birth" in t) {
            return if (any("place", "city", "country", "state", "town", "certificate", "order", "weight", "time")) {
                Keyword.None
            } else {
                Keyword.Match(BIRTH_DATE)
            }
        }

        // Names
        val first = has("first", "name") || any("firstname", "fname", "givenname", "forename") || has("given", "name")
        val last = has("last", "name") || any("lastname", "lname", "surname", "familyname") || has("family", "name")
        val full = has("full", "name") || any("fullname", "yourname", "legalname") ||
            has("your", "name") || has("legal", "name")
        val nameish = first || last || full || t == setOf("name")
        if (nameish) {
            if (t.any { it in NAME_NEGATIVE }) return Keyword.None
            return when {
                first && last -> Keyword.Match(FULL_NAME)
                first -> Keyword.Match(FIRST_NAME)
                last -> Keyword.Match(LAST_NAME)
                full -> Keyword.Match(FULL_NAME)
                // A bare "Name" is far too generic on its own.
                else -> Keyword.Match(FULL_NAME, weight = 30, contextKinds = CONTACT_CONTEXT)
            }
        }

        // Address and its parts
        if (t.any { it in ADDRESS_NEGATIVE }) return Keyword.None

        if (any("apartment", "apt", "suite", "unit", "flat", "floor", "building", "address2", "addressline2",
                "line2", "street2") || has("address", "line", "2") || has("address", "2")
        ) return Keyword.Match(STREET2)

        if (any("street", "streetaddress", "address1", "addressline1", "street1", "line1") ||
            has("address", "line", "1") || has("address", "1") || has("address", "line")
        ) return Keyword.Match(STREET)

        if (any("city", "town", "locality", "municipality", "suburb", "village")) return Keyword.Match(CITY)
        if (any("state", "province", "region", "county", "prefecture")) return Keyword.Match(REGION)
        if (any("zip", "zipcode", "postal", "postalcode", "postcode", "pincode") ||
            has("zip", "code") || has("post", "code")
        ) return Keyword.Match(POSTAL_CODE)
        if (any("country", "nation", "countryname")) return Keyword.Match(COUNTRY)

        if (any("address", "postaladdress", "homeaddress", "billingaddress", "shippingaddress",
                "deliveryaddress", "mailingaddress", "residentialaddress")
        ) return Keyword.Match(ADDRESS_FULL)

        return Keyword.None
    }
}
