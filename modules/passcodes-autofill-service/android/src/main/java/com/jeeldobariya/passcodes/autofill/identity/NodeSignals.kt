package com.jeeldobariya.passcodes.autofill.identity

/** How the view itself says it wants to be typed into (mapped from Android's InputType). */
enum class InputKind { PLAIN, EMAIL, PHONE, PERSON_NAME, POSTAL_ADDRESS, NUMBER, DATETIME, PASSWORD }

/**
 * Everything the classifier looks at for one form field, flattened into
 * plain data so the classifier needs no Android classes (and can be
 * tested off-device).
 */
data class NodeSignals(
    /** android:autofillHints values. */
    val hints: List<String> = emptyList(),
    /** HTML autocomplete attribute (possibly several tokens). */
    val htmlAutocomplete: String? = null,
    /** HTML input type attribute, lowercased. */
    val htmlType: String? = null,
    /** Identifier-style text: view id entry, HTML id / name. */
    val idTexts: List<String> = emptyList(),
    /** Human-facing text: hint, placeholder, content description, label above the field. */
    val labelTexts: List<String> = emptyList(),
    val inputKind: InputKind = InputKind.PLAIN
)

/**
 * The classifier's verdict for one field.
 *
 * [confidence] is 0–100. A candidate below its kind's minimum can still
 * be promoted by [contextKinds]: e.g. a bare "Name" label is only
 * trusted when the same form also has an email or phone field.
 */
data class Classification(
    val kind: IdentityFieldKind,
    val confidence: Int,
    /** True when an explicit autofill hint / HTML autocomplete decided it. */
    val explicit: Boolean,
    val contextKinds: Set<IdentityFieldKind> = emptySet()
)
