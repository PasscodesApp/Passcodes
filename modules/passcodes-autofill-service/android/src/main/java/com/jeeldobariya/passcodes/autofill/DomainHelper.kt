package com.jeeldobariya.passcodes.autofill

/**
 * Stateless helpers for normalizing domain-like strings so values coming
 * from very different sources — a web page's domain, an app's package
 * name, a custom URI scheme — can be compared and stored consistently.
 *
 * An `object` rather than a class: there's no state to hold, and this
 * way every caller (database, autofill service, future UI code) shares
 * one instance without needing to construct anything.
 */
object DomainHelper {

    // Matches any URI scheme:
    // myapp://
    // passcodesapp://
    // intent://
    // https://
    // http://
    private val SCHEME_REGEX =
        Regex("^[a-zA-Z][a-zA-Z0-9+.\\-]*://")

    /**
     * Normalizes a raw domain-like string:
     *
     * - strips any URI scheme
     * - strips leading "www."
     * - strips path / query / fragment
     * - strips port number
     * - strips trailing dot
     * - lowercases
     *
     * Returns null if nothing usable remains.
     */
    fun normalizeDomain(raw: String?): String? {
        if (raw.isNullOrBlank()) return null

        var value = raw.trim()
            .replace(SCHEME_REGEX, "")

        value = value.removePrefix("www.")

        value = value
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")

        value = value.substringBefore(":") // trailing port

        value = value
            .trim()
            .trimEnd('.')
            .lowercase()

        return value.takeIf { it.isNotBlank() }
    }

    /**
     * Stable placeholder URL for entries with no real one, matching
     * Passcodes' existing "https://local.<name>" convention.
     */
    fun buildFallbackUrl(domain: String): String {
        return "https://local.$domain"
    }
}
