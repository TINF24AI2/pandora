package app.pandorapass.pandora.logic.utils

import android.app.assist.AssistStructure
import android.view.autofill.AutofillId

/**
 * Parses a structure (the view when opening entering an typable field)
 */
object StructureParser {
     data class ParsedStructure(
        var usernameId: AutofillId? = null,
        var passwordId: AutofillId? = null,
        var webDomain: String? = null,
        var usernameNode: AssistStructure.ViewNode? = null,
        var passwordNode: AssistStructure.ViewNode? = null
    )

    /**
     * Parses the current structure (the view when opening entering an typable field) to look for
     * relevant fields (fields that can be auto-completed).
     *
     * @param structure The structure
     * @return A parsed structure containing the relevant fields.
     */
     fun parseStructure(structure: AssistStructure): ParsedStructure {
        val res = ParsedStructure()
        val nodes = structure.windowNodeCount
        for (i in 0 until nodes) {
            traverseNode(structure.getWindowNodeAt(i).rootViewNode, res)
        }

        return res
    }

    /**
     * Performs tree-traversal on the structure to find relevant fields.
     *
     * @param node The current node in the structure
     * @param res The parsed structure
     */
    private fun traverseNode(node: AssistStructure.ViewNode, res: ParsedStructure) {
        // If the view is a webpage, we want to save the domain in order to save it later when creating a new Pandora entry
        if (node.webDomain != null) {
            res.webDomain = node.webDomain
        }

        // If the view is a web view, we need to check the HTML info for any fields that look like something we can auto-complete.
        // This works nowhere perfect, but we tried our best to cover as many cases as possible.
        val htmlInfo = node.htmlInfo
        if (htmlInfo != null && "input" == htmlInfo.tag) {
            val attributes = htmlInfo.attributes ?: return
            for (pair in attributes) {
                val value = pair.second.lowercase()
                if (value.contains("email") || value.contains("user") || value.contains("login")) {
                    if (res.usernameId == null) {
                        res.usernameId = node.autofillId
                        res.usernameNode = node
                    }
                }
                if (value.contains("password") || value.contains("pass")) {
                    res.passwordId = node.autofillId
                    res.passwordNode = node
                }
            }
        }

        // If we deal with a native Android view (or some browsers also parse their HTML as Android views),
        // we need to check for the classes
        val className = node.className ?: ""
        if (className.contains("EditText") || className.contains("TextInput")) {
            checkViewProperties(node, res)
        }

        // Yeah, and the same stuff again for every child node.
        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), res)
        }
    }

    private fun checkViewProperties(node: AssistStructure.ViewNode, res: ParsedStructure) {
        // Take any info we can find and hope that we get some info out of that
        val idEntry = node.idEntry?.lowercase() ?: ""
        val hintText = node.hint?.lowercase() ?: ""
        val inputType = node.inputType
        val autofillHints = node.autofillHints

        // We need to decode the input type
        val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION

        // Check if what we found is a username field (or email)
        var isUsername = false

        // If the type already matches something that's an email, we can assume it's an username
        if (variation == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) {
            isUsername = true
        }

        // If the field's id matches something that's a username, we also assume it's an username
        if (idEntry.contains("email") || idEntry.contains("user") || idEntry.contains("login")) {
            isUsername = true
        }

        // If the field's hint text is set and matches something that's a username, we also assume it's an username
        if (hintText.contains("email") || hintText.contains("user") || hintText.contains("login")) {
            isUsername = true
        }

        // If any autofill hints are set and match something that's a username, we also assume it's an username
        if (autofillHints?.any { it.contains("email", true) || it.contains("username", true) } == true) {
            isUsername = true
        }

        // If anything looked like it could be a username, we assume it is one and suggest the autofill.
        if (isUsername && res.usernameId == null) {
            res.usernameId = node.autofillId
            res.usernameNode = node
        }

        // Aaaand the same thing for passwords. You get the point.
        var isPassword = false

        if (variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
            isPassword = true
        }

        if (idEntry.contains("password") || idEntry.contains("pass")) isPassword = true
        if (hintText.contains("password") || hintText.contains("pass")) isPassword = true
        if (autofillHints?.any {
            it.contains("password", true) || it.contains("pass", true)
        } == true) {
            isPassword = true
        }

        if (isPassword) {
            res.passwordId = node.autofillId
            res.passwordNode = node
        }

    }
}