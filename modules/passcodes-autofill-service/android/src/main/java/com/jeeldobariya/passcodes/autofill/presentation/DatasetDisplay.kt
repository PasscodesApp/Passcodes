package com.jeeldobariya.passcodes.autofill.presentation

import androidx.annotation.DrawableRes

/**
 * Everything the UI needs to draw one suggestion, whatever it fills
 * (a saved login or personal details). Contains display text only —
 * for a locked suggestion that text must never include the real values.
 */
data class DatasetDisplay(
    /** Dropdown: first (bold) line. */
    val title: String,
    /** Dropdown: second line. */
    val subtitle: String,
    /** Keyboard chip: title / subtitle (chips lead with the distinguishing text). */
    val inlineTitle: String,
    val inlineSubtitle: String,
    @DrawableRes val iconRes: Int,
    /** Unique per suggestion; used as the request code of the chip's PendingIntent. */
    val requestKey: Int
)
