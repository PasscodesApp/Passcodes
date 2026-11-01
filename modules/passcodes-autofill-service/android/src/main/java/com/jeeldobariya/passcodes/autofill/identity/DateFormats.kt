package com.jeeldobariya.passcodes.autofill.identity

/**
 * Works out how a plain-text date field wants its date written, from
 * the hint text ("DD/MM/YYYY", "yyyy-mm-dd"…) or an HTML date input.
 * Returns null when it can't tell — we then skip the field rather than
 * guess between 03/04 and 04/03.
 */
object DateFormats {

    private val PATTERN = Regex(
        """(?i)\b(dd|mm|yyyy|yy)([/.\- ])(dd|mm|yyyy|yy)\2(dd|mm|yyyy|yy)\b"""
    )

    fun infer(texts: List<String>, htmlType: String?): String? {
        if (htmlType == "date") return "yyyy-MM-dd"
        for (text in texts) {
            val m = PATTERN.find(text) ?: continue
            val parts = listOf(m.groupValues[1], m.groupValues[3], m.groupValues[4]).map { it.lowercase() }
            val separator = m.groupValues[2]
            val hasDay = parts.count { it == "dd" } == 1
            val hasMonth = parts.count { it == "mm" } == 1
            val hasYear = parts.count { it == "yyyy" || it == "yy" } == 1
            if (!(hasDay && hasMonth && hasYear)) continue
            return parts.joinToString(separator) {
                when (it) {
                    "dd" -> "dd"
                    "mm" -> "MM"
                    else -> it
                }
            }
        }
        return null
    }

    /** Formats an ISO "yyyy-MM-dd" date with a pattern from [infer]. Null if either is invalid. */
    fun format(isoDate: String, pattern: String): String? {
        val m = Regex("""^(\d{4})-(\d{2})-(\d{2})$""").matchEntire(isoDate) ?: return null
        val (year, month, day) = m.destructured
        if (!Regex("""^(dd|MM|yyyy|yy)([/.\- ])(dd|MM|yyyy|yy)\2(dd|MM|yyyy|yy)$""").matches(pattern)) return null
        val separator = pattern.first { it in "/.- " }.toString()
        return pattern.split(separator).joinToString(separator) {
            when (it) {
                "dd" -> day
                "MM" -> month
                "yyyy" -> year
                else -> year.takeLast(2)
            }
        }
    }
}
