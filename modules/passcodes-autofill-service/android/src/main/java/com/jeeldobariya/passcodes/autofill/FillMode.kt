package com.jeeldobariya.passcodes.autofill

/**
 * Which saved credentials the fill picker offers for a form.
 *
 *  - [SMART] only credentials matching the site/app. If nothing
 *    matches, nothing is shown (the Save prompt still works).
 *  - [AUTO] (default) matching credentials first; if none match, fall
 *    back to showing every saved credential.
 *  - [DUMB] always every saved credential, newest first.
 */
enum class FillMode(val storageValue: String) {
    SMART("smart"),
    AUTO("auto"),
    DUMB("dumb");

    companion object {
        val DEFAULT = AUTO

        /** Unknown / missing values (older installs, corrupted prefs) fall back to [DEFAULT]. */
        fun fromStorage(value: String?): FillMode =
            entries.firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}
