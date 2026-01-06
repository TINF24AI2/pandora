package app.pandorapass.pandora.logic.utils

import android.app.assist.AssistStructure
import android.util.Log

object StructurePrinter {
     fun printStructure(structure: AssistStructure) {
        val nodes = structure.windowNodeCount
        Log.d("AutofillDebug", "Structure contains $nodes windows")
        for (i in 0 until nodes) {
            val root = structure.getWindowNodeAt(i).rootViewNode
            printNode(root, 0)
        }
    }

     fun printNode(node: AssistStructure.ViewNode, depth: Int) {
        val indent = "  ".repeat(depth)
        val id = node.idEntry ?: "no-id"
        val hints = node.autofillHints?.joinToString() ?: "no-hints"
        val className = node.className
        val text = node.text ?: ""
        val hintText = node.hint ?: ""

        // Log relevant nodes only to reduce noise
        if (className!!.contains("EditText") || className.contains("Input")) {
            Log.d("AutofillDebug", "$indent Field: ID=$id | Class=$className | Hints=$hints | HintText=$hintText | InputType=${node.inputType}")
        }

        for (i in 0 until node.childCount) {
            printNode(node.getChildAt(i), depth + 1)
        }
    }
}