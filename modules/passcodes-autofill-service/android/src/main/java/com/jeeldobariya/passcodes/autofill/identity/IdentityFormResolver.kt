package com.jeeldobariya.passcodes.autofill.identity

/**
 * Looks at every classified field on a screen together and decides
 * which ones we will actually offer to fill. Generic over the field
 * handle [T] (an AutofillId in the service, a string in tests).
 */
object IdentityFormResolver {

    data class Entry<T>(val key: T, val classification: Classification, val focused: Boolean)

    data class Resolved<T>(val key: T, val kind: IdentityFieldKind, val focused: Boolean, val confidence: Int)

    /** A manual "Autofill" request on a field is the user explicitly asking: lower the bar a little. */
    private const val MANUAL_RELAXATION = 15
    private const val CONTEXT_BOOST = 25
    private const val MAX_EMAIL_FIELDS = 2

    fun <T> resolve(entries: List<Entry<T>>, manualRequest: Boolean): List<Resolved<T>> {
        val confidence = entries.map { it.classification.confidence }.toMutableList()
        val accepted = BooleanArray(entries.size)

        fun threshold(i: Int): Int {
            val relax = if (manualRequest && entries[i].focused) MANUAL_RELAXATION else 0
            return entries[i].classification.kind.minConfidence - relax
        }

        for (i in entries.indices) accepted[i] = confidence[i] >= threshold(i)

        // Weak candidates (a bare "Name", a bare "Country code") are
        // promoted only by neighbours we already trust.
        for (i in entries.indices) {
            if (accepted[i]) continue
            val context = entries[i].classification.contextKinds
            if (context.isEmpty()) continue
            val supported = entries.indices.any { j ->
                j != i && accepted[j] && entries[j].classification.kind in context
            }
            if (supported) {
                confidence[i] += CONTEXT_BOOST
                accepted[i] = confidence[i] >= threshold(i)
            }
        }

        // A plain "Address" is the street line when the form has separate
        // city / state / postal / country fields.
        val addressParts = setOf(
            IdentityFieldKind.CITY, IdentityFieldKind.REGION,
            IdentityFieldKind.POSTAL_CODE, IdentityFieldKind.COUNTRY
        )
        val hasSeparateParts = entries.indices.any { accepted[it] && entries[it].classification.kind in addressParts }
        val kinds = entries.mapIndexed { i, e ->
            val k = e.classification.kind
            if (accepted[i] && k == IdentityFieldKind.ADDRESS_FULL && !e.classification.explicit && hasSeparateParts) {
                IdentityFieldKind.STREET
            } else {
                k
            }
        }

        // One field per kind (best wins), except up to two email fields
        // ("Email" + "Confirm email").
        val keep = BooleanArray(entries.size)
        for ((kind, indices) in entries.indices.filter { accepted[it] }.groupBy { kinds[it] }) {
            val ranked = indices.sortedWith(
                compareByDescending<Int> { entries[it].focused }.thenByDescending { confidence[it] }.thenBy { it }
            )
            val limit = if (kind == IdentityFieldKind.EMAIL) MAX_EMAIL_FIELDS else 1
            ranked.take(limit).forEach { keep[it] = true }
        }

        return entries.indices.filter { keep[it] }.map {
            Resolved(entries[it].key, kinds[it], entries[it].focused, confidence[it])
        }
    }
}
