package com.jeeldobariya.passcodes.autofill.parser

import android.app.assist.AssistStructure
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId
import androidx.autofill.HintConstants
import com.jeeldobariya.passcodes.autofill.identity.Classification
import com.jeeldobariya.passcodes.autofill.identity.DateFormats
import com.jeeldobariya.passcodes.autofill.identity.FieldSpec
import com.jeeldobariya.passcodes.autofill.identity.FieldTraits
import com.jeeldobariya.passcodes.autofill.identity.IdentityFieldClassifier
import com.jeeldobariya.passcodes.autofill.identity.IdentityFormResolver
import com.jeeldobariya.passcodes.autofill.identity.InputKind
import com.jeeldobariya.passcodes.autofill.identity.NodeSignals

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
        val distinctWebDomains: Set<String>,
        /** Personal-detail candidates (email, phone, name, address…), already classified. */
        val identityNodes: List<IdentityNode> = emptyList(),
        /** How many distinct password-type inputs the screen has. */
        val passwordNodeCount: Int = 0
    ) {
        fun node(type: AutofillFieldType) = fields[type]?.node
        fun autofillId(type: AutofillFieldType) = fields[type]?.node?.autofillId

        /**
         * A sign-in screen: one current-password field and no new-password
         * field. There the saved credentials are what's wanted, so personal
         * details (an "email" box is also a username box) are not offered.
         */
        val isLoginForm: Boolean
            get() = fields.containsKey(AutofillFieldType.PASSWORD) &&
                !fields.containsKey(AutofillFieldType.NEW_PASSWORD) &&
                passwordNodeCount < 2

        /** The personal-detail fields we're confident enough to offer to fill. */
        fun resolveIdentityFields(manualRequest: Boolean): List<IdentityField> =
            IdentityFormResolver.resolve(
                identityNodes.map { IdentityFormResolver.Entry(it, it.classification, it.focused) },
                manualRequest
            ).map { resolved ->
                IdentityField(
                    autofillId = resolved.key.autofillId,
                    spec = FieldSpec(resolved.kind, resolved.key.traits),
                    focused = resolved.focused
                )
            }
    }

    /** A form field classified as a personal detail, before the form-wide decision. */
    data class IdentityNode(
        val autofillId: AutofillId,
        val classification: Classification,
        val traits: FieldTraits,
        val focused: Boolean
    )

    /** A field we will offer to fill. */
    data class IdentityField(val autofillId: AutofillId, val spec: FieldSpec, val focused: Boolean)

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
        val identity = mutableListOf<IdentityNode>()
        val webDomains = mutableSetOf<String>()
        var webScheme: String? = null

        for (windowIndex in 0 until structure.windowNodeCount) {
            val root = structure.getWindowNodeAt(windowIndex).rootViewNode
            webScheme = webScheme ?: root.webScheme
            walk(root, candidates, identity, webDomains, precedingLabel = null)
        }

        val best = pickBestPerType(candidates)

        return ParsedAutofillRequest(
            packageName = packageName,
            webDomain = webDomains.firstOrNull(),
            webScheme = webScheme,
            fields = best,
            distinctWebDomains = webDomains,
            identityNodes = identity,
            passwordNodeCount = candidates
                .filter { it.type == AutofillFieldType.PASSWORD }
                .map { it.node.autofillId }
                .distinct()
                .size
        )
    }

    // ---------------------------------------------------------------
    // Tree walk
    // ---------------------------------------------------------------

    private fun walk(
        node: AssistStructure.ViewNode,
        candidates: MutableList<FieldCandidate>,
        identity: MutableList<IdentityNode>,
        webDomains: MutableSet<String>,
        precedingLabel: String?
    ) {
        node.webDomain?.let { webDomains.add(it) }

        if (isFillable(node)) {
            collectCandidates(node).forEach(candidates::add)
            classifyIdentity(node, precedingLabel)?.let(identity::add)
        }

        // Static text just before an input is usually its label. Only
        // text from non-input views is used — an input's own text is
        // what the user typed, and is never read here.
        var label: String? = null
        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i)
            walk(child, candidates, identity, webDomains, label)

            val text = child.text?.toString()?.trim()
            label = when {
                child.autofillType == View.AUTOFILL_TYPE_NONE && child.childCount == 0 &&
                    !text.isNullOrBlank() && text.length <= MAX_LABEL_LENGTH -> text
                isFillable(child) -> null
                else -> label
            }
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

    // ---------------------------------------------------------------
    // Personal-detail classification
    // ---------------------------------------------------------------

    private fun classifyIdentity(node: AssistStructure.ViewNode, precedingLabel: String?): IdentityNode? {
        // Only text, list and date inputs can take the values we have.
        val type = node.autofillType
        if (type != View.AUTOFILL_TYPE_TEXT && type != View.AUTOFILL_TYPE_LIST && type != View.AUTOFILL_TYPE_DATE) return null

        val attrs = htmlAttributes(node)
        val signals = NodeSignals(
            hints = node.autofillHints?.toList().orEmpty(),
            htmlAutocomplete = attrs["autocomplete"],
            htmlType = attrs["type"]?.lowercase(),
            idTexts = listOfNotNull(node.idEntry, attrs["id"], attrs["name"]),
            labelTexts = listOfNotNull(
                node.hint?.toString(),
                attrs["placeholder"],
                attrs["aria-label"],
                node.contentDescription?.toString(),
                precedingLabel
            ),
            inputKind = inputKindOf(node.inputType)
        )

        val classification = IdentityFieldClassifier.classify(signals) ?: return null
        val autofillId = node.autofillId ?: return null

        val traits = FieldTraits(
            acceptsDate = type == View.AUTOFILL_TYPE_DATE,
            // A list field with unreadable options gets an empty list, so
            // nothing can match and the field is skipped.
            listOptions = if (type == View.AUTOFILL_TYPE_LIST) {
                node.autofillOptions?.map { it.toString() }.orEmpty()
            } else {
                null
            },
            numericOnly = signals.inputKind == InputKind.NUMBER,
            maxLength = attrs["maxlength"]?.trim()?.toIntOrNull()?.takeIf { it > 0 },
            dateFormat = DateFormats.infer(signals.labelTexts, signals.htmlType)
        )

        return IdentityNode(autofillId, classification, traits, node.isFocused)
    }

    private fun htmlAttributes(node: AssistStructure.ViewNode): Map<String, String> {
        val html = node.htmlInfo ?: return emptyMap()
        val tag = html.tag?.trim()?.lowercase()
        if (tag != "input" && tag != "select" && tag != "textarea") return emptyMap()
        return html.attributes
            ?.associate { it.first.trim().lowercase() to it.second.trim() }
            .orEmpty()
    }

    private fun inputKindOf(inputType: Int): InputKind {
        if (isPasswordInputType(inputType)) return InputKind.PASSWORD
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_PHONE -> InputKind.PHONE
            InputType.TYPE_CLASS_NUMBER -> InputKind.NUMBER
            InputType.TYPE_CLASS_DATETIME -> InputKind.DATETIME
            InputType.TYPE_CLASS_TEXT -> when (variation) {
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> InputKind.EMAIL
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME -> InputKind.PERSON_NAME
                InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS -> InputKind.POSTAL_ADDRESS
                else -> InputKind.PLAIN
            }
            else -> InputKind.PLAIN
        }
    }

    private fun isPasswordInputType(inputType: Int): Boolean {
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        // The variation constants overlap between classes (a number
        // "password" has the same value as a text "URI"), so the class
        // must be checked too — otherwise URL fields look like passwords.
        return when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_TEXT ->
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
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

    private companion object {
        /** Longest static text still treated as a field label. */
        const val MAX_LABEL_LENGTH = 40
    }
}
