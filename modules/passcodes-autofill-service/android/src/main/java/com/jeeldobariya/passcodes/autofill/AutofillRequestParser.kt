package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure
import android.text.InputType
import android.view.View
import androidx.autofill.HintConstants

/**
 * Parses an Autofill [AssistStructure] into the fields we care about.
 *
 * Instead of taking the first node that looks like a match, every node
 * is scored as a candidate and the highest-confidence candidate per
 * field type wins. This avoids false positives from unrelated fields
 * (hidden inputs, disabled fields, unrelated text boxes) stealing a
 * slot from the real username/password field.
 */
class AutofillRequestParser {

    data class ParsedAutofillRequest(
        val packageName: String?,
        val webDomain: String?,
        val webScheme: String?,
        /** Best pick per field type. */
        val fields: Map<AutofillFieldType, FieldCandidate>,
        /** All web domains seen across nodes — >1 distinct value can indicate
         *  a cross-origin iframe, which is worth treating cautiously. */
        val distinctWebDomains: Set<String>
    ) {
        fun node(type: AutofillFieldType) = fields[type]?.node
        fun autofillId(type: AutofillFieldType) = fields[type]?.node?.autofillId
    }

    data class FieldCandidate(
        val node: AssistStructure.ViewNode,
        val type: AutofillFieldType,
        val source: HintSource,
        val isFocused: Boolean
    ) {
        val confidence: Int get() = source.weight + if (isFocused) FOCUS_BONUS else 0
        companion object { private const val FOCUS_BONUS = 5 }
    }

    enum class AutofillFieldType { USERNAME, PASSWORD, NEW_PASSWORD }

    /** Higher weight = more trustworthy signal. Ties broken by focus. */
    enum class HintSource(val weight: Int) {
        ANDROID_AUTOFILL_HINT(100),   // explicit android:autofillHints
        HTML_AUTOCOMPLETE(90),        // autocomplete="username" / "current-password" etc.
        HTML_TYPE_OR_NAME(80),        // <input type="password"> / name="password"
        ANDROID_INPUT_TYPE(60),       // TYPE_TEXT_VARIATION_PASSWORD
        VIEW_ID_OR_HINT_TEXT(40)      // idEntry/hint text contains "password"/"username"
    }

    private val usernameHints = setOf(
        HintConstants.AUTOFILL_HINT_USERNAME,
        HintConstants.AUTOFILL_HINT_EMAIL_ADDRESS
    )

    fun parse(structure: AssistStructure): ParsedAutofillRequest {
        val packageName = structure.activityComponent?.packageName
        val candidates = mutableListOf<FieldCandidate>()
        val webDomains = mutableSetOf<String>()
        var webScheme: String? = null

        for (windowIndex in 0 until structure.windowNodeCount) {
            val root = structure.getWindowNodeAt(windowIndex).rootViewNode
            webScheme = webScheme ?: root.webScheme
            walk(root, candidates, webDomains)
        }

        val best = pickBestPerType(candidates)

        return ParsedAutofillRequest(
            packageName = packageName,
            webDomain = webDomains.firstOrNull(),
            webScheme = webScheme,
            fields = best,
            distinctWebDomains = webDomains
        )
    }

    // ---------------------------------------------------------------
    // Tree walk
    // ---------------------------------------------------------------

    private fun walk(
        node: AssistStructure.ViewNode,
        candidates: MutableList<FieldCandidate>,
        webDomains: MutableSet<String>
    ) {
        node.webDomain?.let { webDomains.add(it) }

        if (isFillable(node)) {
            collectCandidates(node).forEach(candidates::add)
        }

        for (i in 0 until node.childCount) {
            walk(node.getChildAt(i), candidates, webDomains)
        }
    }

    /** Skip nodes that can never sensibly receive an autofill value. */
    private fun isFillable(node: AssistStructure.ViewNode): Boolean {
        if (node.autofillId == null) return false
        if (node.autofillType == View.AUTOFILL_TYPE_NONE) return false
        if (node.visibility != View.VISIBLE) return false
        if (!node.isEnabled) return false
        val importance = node.importantForAutofill
        if (importance == View.IMPORTANT_FOR_AUTOFILL_NO ||
            importance == View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS) return false
        return true
    }

    // ---------------------------------------------------------------
    // Per-node candidate detection — every applicable signal is recorded,
    // scoring sorts out which one to trust later.
    // ---------------------------------------------------------------

    private fun collectCandidates(node: AssistStructure.ViewNode): List<FieldCandidate> {
        val focused = node.isFocused
        val found = mutableListOf<FieldCandidate>()

        // 1. Official Android autofill hints — most trustworthy signal.
        node.autofillHints?.forEach { hint ->
            when {
                hint == HintConstants.AUTOFILL_HINT_NEW_PASSWORD ->
                    found += FieldCandidate(node, AutofillFieldType.NEW_PASSWORD, HintSource.ANDROID_AUTOFILL_HINT, focused)
                hint == HintConstants.AUTOFILL_HINT_PASSWORD ->
                    found += FieldCandidate(node, AutofillFieldType.PASSWORD, HintSource.ANDROID_AUTOFILL_HINT, focused)
                hint in usernameHints ->
                    found += FieldCandidate(node, AutofillFieldType.USERNAME, HintSource.ANDROID_AUTOFILL_HINT, focused)
            }
        }

        // 2. HTML attributes (WebView forms).
        node.htmlInfo?.let { html ->
            if (html.tag?.trim()?.equals("input", ignoreCase = true) == true) {
                val attrs = html.attributes
                    ?.associate { it.first.trim().lowercase() to it.second.trim().lowercase() }
                    .orEmpty()

                val type = attrs["type"]
                val autocomplete = attrs["autocomplete"]
                val name = attrs["name"]

                when {
                    autocomplete == "new-password" ->
                        found += FieldCandidate(node, AutofillFieldType.NEW_PASSWORD, HintSource.HTML_AUTOCOMPLETE, focused)
                    autocomplete == "current-password" ->
                        found += FieldCandidate(node, AutofillFieldType.PASSWORD, HintSource.HTML_AUTOCOMPLETE, focused)
                    autocomplete == "username" || autocomplete == "email" ->
                        found += FieldCandidate(node, AutofillFieldType.USERNAME, HintSource.HTML_AUTOCOMPLETE, focused)

                    type == "password" || name == "password" ->
                        found += FieldCandidate(node, AutofillFieldType.PASSWORD, HintSource.HTML_TYPE_OR_NAME, focused)
                    name in setOf("username", "user", "email") ->
                        found += FieldCandidate(node, AutofillFieldType.USERNAME, HintSource.HTML_TYPE_OR_NAME, focused)
                }
            }
        }

        // 3. Native Android input type.
        if (isPasswordInputType(node.inputType)) {
            found += FieldCandidate(node, AutofillFieldType.PASSWORD, HintSource.ANDROID_INPUT_TYPE, focused)
        }

        // 4. Weakest signal: view id / hint text guessing.
        val hintText = node.hint?.toString()?.trim()?.lowercase()
        val idEntry = node.idEntry?.trim()?.lowercase()
        val idOrHintSuggestsPassword = listOf(hintText, idEntry).any {
            it == "password" || it?.contains("password") == true
        }
        val idOrHintSuggestsUsername = listOf(hintText, idEntry).any {
            it == "username" || it == "email" ||
                it?.contains("username") == true || it?.contains("email") == true
        }
        if (idOrHintSuggestsPassword) {
            found += FieldCandidate(node, AutofillFieldType.PASSWORD, HintSource.VIEW_ID_OR_HINT_TEXT, focused)
        } else if (idOrHintSuggestsUsername) {
            found += FieldCandidate(node, AutofillFieldType.USERNAME, HintSource.VIEW_ID_OR_HINT_TEXT, focused)
        }

        return found
    }

    private fun isPasswordInputType(inputType: Int): Boolean {
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    // ---------------------------------------------------------------
    // Resolve: highest-confidence candidate wins per type.
    // ---------------------------------------------------------------

    private fun pickBestPerType(
        candidates: List<FieldCandidate>
    ): Map<AutofillFieldType, FieldCandidate> {
        return candidates
            .groupBy { it.type }
            .mapValues { (_, group) -> group.maxBy { it.confidence } }
    }
}
