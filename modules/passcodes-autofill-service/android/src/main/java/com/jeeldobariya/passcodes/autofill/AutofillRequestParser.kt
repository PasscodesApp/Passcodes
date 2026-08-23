package com.jeeldobariya.passcodes.autofill

import android.app.assist.AssistStructure
import android.text.InputType
import androidx.autofill.HintConstants

class AutofillRequestParser {

    data class ParsedAutofillRequest(
        val fields: Map<AutofillFieldType, AssistStructure.ViewNode>,
        val webDomain: String?,
        val webScheme: String?,
        val hasHtmlNodes: Boolean
    )

    enum class AutofillFieldType {
        USERNAME,
        PASSWORD
    }

    private val usernameHints = setOf(
        HintConstants.AUTOFILL_HINT_USERNAME,
        HintConstants.AUTOFILL_HINT_EMAIL_ADDRESS
    )

    private val passwordHints = setOf(
        HintConstants.AUTOFILL_HINT_PASSWORD,
        HintConstants.AUTOFILL_HINT_NEW_PASSWORD
    )

    fun parse(
        structure: AssistStructure
    ): ParsedAutofillRequest {

        val fields = mutableMapOf<
            AutofillFieldType,
            AssistStructure.ViewNode
        >()

        var webDomain: String? = null
        var webScheme: String? = null
        var hasHtmlNodes = false

        for (windowIndex in 0 until structure.windowNodeCount) {

            val rootNode =
                structure
                    .getWindowNodeAt(windowIndex)
                    .rootViewNode

            val result = parseNode(
                node = rootNode,
                fields = fields
            )

            if (webDomain == null) {
                webDomain = result.webDomain
            }

            if (webScheme == null) {
                webScheme = result.webScheme
            }

            if (result.hasHtmlNodes) {
                hasHtmlNodes = true
            }
        }

        return ParsedAutofillRequest(
            fields = fields,
            webDomain = webDomain,
            webScheme = webScheme,
            hasHtmlNodes = hasHtmlNodes
        )
    }

    private data class NodeParseResult(
        val webDomain: String?,
        val webScheme: String?,
        val hasHtmlNodes: Boolean
    )

    private fun parseNode(
        node: AssistStructure.ViewNode,
        fields: MutableMap<
            AutofillFieldType,
            AssistStructure.ViewNode
        >
    ): NodeParseResult {

        var webDomain: String? = null
        var webScheme: String? = null
        var hasHtmlNodes = false

        // ------------------------------------------------------------
        // 1. Website information
        // ------------------------------------------------------------

        if (node.webDomain != null) {
            webDomain = node.webDomain
        }

        if (node.webScheme != null) {
            webScheme = node.webScheme
        }

        // ------------------------------------------------------------
        // 2. HTML information
        // ------------------------------------------------------------

        if (node.htmlInfo != null) {
            hasHtmlNodes = true
        }

        // ------------------------------------------------------------
        // 3. Official Android Autofill hints
        // ------------------------------------------------------------

        detectFromAutofillHints(
            node = node,
            fields = fields
        )

        // ------------------------------------------------------------
        // 4. Basic fallback detection
        // ------------------------------------------------------------

        detectFromFallbacks(
            node = node,
            fields = fields
        )

        // ------------------------------------------------------------
        // 5. Continue through child nodes
        // ------------------------------------------------------------

        for (index in 0 until node.childCount) {

            val childResult = parseNode(
                node = node.getChildAt(index),
                fields = fields
            )

            if (webDomain == null) {
                webDomain = childResult.webDomain
            }

            if (webScheme == null) {
                webScheme = childResult.webScheme
            }

            if (childResult.hasHtmlNodes) {
                hasHtmlNodes = true
            }
        }

        return NodeParseResult(
            webDomain = webDomain,
            webScheme = webScheme,
            hasHtmlNodes = hasHtmlNodes
        )
    }

    private fun detectFromAutofillHints(
        node: AssistStructure.ViewNode,
        fields: MutableMap<
            AutofillFieldType,
            AssistStructure.ViewNode
        >
    ) {

        node.autofillHints?.forEach { hint ->

            when {
                hint in usernameHints -> {
                    fields.putIfAbsent(
                        AutofillFieldType.USERNAME,
                        node
                    )
                }

                hint in passwordHints -> {
                    fields.putIfAbsent(
                        AutofillFieldType.PASSWORD,
                        node
                    )
                }
            }
        }
    }

    private fun detectFromFallbacks(
        node: AssistStructure.ViewNode,
        fields: MutableMap<
            AutofillFieldType,
            AssistStructure.ViewNode
        >
    ) {

        // Don't try to guess from obviously non-fillable nodes.
        if (node.autofillId == null) {
            return
        }

        // ------------------------------------------------------------
        // HTML fallback
        // ------------------------------------------------------------

        detectFromHtmlInfo(
            node = node,
            fields = fields
        )

        // ------------------------------------------------------------
        // Android input type fallback
        // ------------------------------------------------------------

        if (!fields.containsKey(AutofillFieldType.PASSWORD)) {

            if (isPasswordInput(node)) {
                fields.putIfAbsent(
                    AutofillFieldType.PASSWORD,
                    node
                )
            }
        }

        // ------------------------------------------------------------
        // Android View metadata fallback
        // ------------------------------------------------------------

        detectFromViewMetadata(
            node = node,
            fields = fields
        )
    }

    private fun detectFromHtmlInfo(
        node: AssistStructure.ViewNode,
        fields: MutableMap<
            AutofillFieldType,
            AssistStructure.ViewNode
        >
    ) {

        val htmlInfo = node.htmlInfo ?: return

        val tag = htmlInfo.tag
            ?.trim()
            ?.lowercase()

        if (tag != "input") {
            return
        }

        val attributes = htmlInfo.attributes
            ?.associate {
                it.first
                    .trim()
                    .lowercase() to it.second
            }
            .orEmpty()

        val type = attributes["type"]
            ?.trim()
            ?.lowercase()

        val autocomplete = attributes["autocomplete"]
            ?.trim()
            ?.lowercase()

        val name = attributes["name"]
            ?.trim()
            ?.lowercase()

        // Password
        if (
            type == "password" ||
            autocomplete == "current-password" ||
            autocomplete == "new-password" ||
            name == "password"
        ) {
            fields.putIfAbsent(
                AutofillFieldType.PASSWORD,
                node
            )

            return
        }

        // Username
        if (
            autocomplete == "username" ||
            autocomplete == "email" ||
            name == "username" ||
            name == "user" ||
            name == "email"
        ) {
            fields.putIfAbsent(
                AutofillFieldType.USERNAME,
                node
            )
        }
    }

    private fun isPasswordInput(
        node: AssistStructure.ViewNode
    ): Boolean {

        val inputType = node.inputType

        return (
            inputType and
                (
                    InputType.TYPE_TEXT_VARIATION_PASSWORD or
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                )
            ) != 0
    }

    private fun detectFromViewMetadata(
        node: AssistStructure.ViewNode,
        fields: MutableMap<
            AutofillFieldType,
            AssistStructure.ViewNode
        >
    ) {

        val hint = node.hint
            ?.toString()
            ?.trim()
            ?.lowercase()

        val id = node.idEntry
            ?.trim()
            ?.lowercase()

        // Username / email
        if (
            hint == "username" ||
            hint == "email" ||
            id == "username" ||
            id == "email" ||
            id?.contains("username") == true ||
            id?.contains("email") == true
        ) {
            fields.putIfAbsent(
                AutofillFieldType.USERNAME,
                node
            )

            return
        }

        // Password
        if (
            hint == "password" ||
            id == "password" ||
            id?.contains("password") == true
        ) {
            fields.putIfAbsent(
                AutofillFieldType.PASSWORD,
                node
            )
        }
    }
}
