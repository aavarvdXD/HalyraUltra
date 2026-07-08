package com.aavarvd.halyra.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText

class PythonHighLightTransformation : VisualTransformation {
    val functionDefColor = Color(0xFF56B6C2)
    val classDefColor = Color(0xFFE5C078)
    val functionCallColor = Color(0xFF61AFEF)
    val variableColor = Color(0xFF9CDCFE)

    // Fixed: added * to capture full name (not just first character)
    val classRegex = Regex("\\bclass\\s+([A-Za-z_]\\w*)")
    val defRegex = Regex("\\bdef\\s+([A-Za-z_]\\w*)")
    val callRegex = Regex("\\b([A-Za-z_]\\w*)\\s*\\(")
    val assignRegex = Regex("\\b([A-za-z_]\\w*)\\s*=(?!=)")

    private val keywords = listOf(
        "False", "None", "True",
        "and", "as", "assert", "async", "await",
        "break",
        "class", "continue",
        "def", "del",
        "elif", "else", "except",
        "finally", "for", "from",
        "global",
        "if", "import", "in", "is",
        "lambda",
        "nonlocal", "not",
        "or",
        "pass",
        "raise", "return",
        "try",
        "while", "with",
        "yield",
        "int", "float", "str", "bool",
        "list", "tuple", "dict", "set",
        "bytes", "bytearray", "complex",
        "frozenset", "object", "type",
        "print", "input", "len", "range",
        "enumerate", "zip", "map", "filter",
        "sum", "min", "max", "abs",
        "round", "sorted", "reversed",
        "open", "isinstance", "issubclass",
        "super", "property",
        "staticmethod", "classmethod",
        "Exception", "ValueError",
        "TypeError", "KeyError",
        "IndexError", "AttributeError",
        "RuntimeError", "ImportError",
        "ModuleNotFoundError",
        "FileNotFoundError",
        "ZeroDivisionError",
        "os", "sys", "math", "random",
        "time", "datetime",
        "json", "re", "pathlib",
        "collections", "itertools",
        "functools", "typing",
        "threading", "asyncio",
        "__name__", "__main__"
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val code = text.text
        val highlighted = AnnotatedString.Builder(code)
        val stringRanges = Regex("\".*?\"|'.*?'")
            .findAll(code)
            .map { it.range }
            .toList()

        val commentRanges = Regex("#.*")
            .findAll(code)
            .map { it.range }
            .toList()

        fun isInsideString(index: Int): Boolean {
            return stringRanges.any { index in it }
        }

        fun isInsideComment(index: Int): Boolean {
            return commentRanges.any { index in it }
        }

        Regex("#.*")
            .findAll(code)
            .forEach {
                highlighted.addStyle(
                    SpanStyle(color = Color(0xFF808080)),
                    it.range.first,
                    it.range.last + 1
                )
            }

        Regex("\".*?\"|'.*?'")
            .findAll(code)
            .forEach {
                highlighted.addStyle(
                    SpanStyle(color = Color(0xFF6A8759)),
                    it.range.first,
                    it.range.last + 1
                )
            }

        // Apply class highlighting BEFORE function highlighting
        // so that class names take precedence
        classRegex.findAll(code).forEach { match ->
            val range = match.groups[1]?.range ?: return@forEach

            if (!isInsideString(range.first) && !isInsideComment(range.first)) {
                highlighted.addStyle(
                    SpanStyle(color = classDefColor),
                    range.first,
                    range.last + 1
                )
            }
        }

        defRegex.findAll(code).forEach { match ->
            val range = match.groups[1]?.range ?: return@forEach

            if (!isInsideString(range.first) && !isInsideComment(range.first)) {
                highlighted.addStyle(
                    SpanStyle(color = functionDefColor),
                    range.first,
                    range.last + 1
                )
            }
        }

        callRegex.findAll(code).forEach { match ->
            val name = match.groups[1]?.value ?: return@forEach
            val start = match.groups[1]?.range?.first ?: return@forEach

            if (!isInsideString(start) && !isInsideComment(start) && name !in keywords) {
                highlighted.addStyle(
                    SpanStyle(color = functionCallColor),
                    start,
                    start + name.length
                )
            }
        }

        assignRegex.findAll(code).forEach { match ->
            val start = match.groups[1]?.range?.first ?: return@forEach

            if (!isInsideString(start) && !isInsideComment(start)) {
                highlighted.addStyle(
                    SpanStyle(color = variableColor),
                    start,
                    start + match.groups[1]!!.value.length
                )
            }
        }

        keywords.forEach { keyword ->
            Regex("\\b$keyword\\b")
                .findAll(code)
                .forEach { match ->
                    if (!isInsideString(match.range.first) && !isInsideComment(match.range.first)) {
                        highlighted.addStyle(
                            SpanStyle(color = Color(0xFFCC7832)),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                }
        }

        Regex("\\b\\d+(\\.\\d+)?\\b")
            .findAll(code)
            .forEach { match ->
                if (!isInsideString(match.range.first) && !isInsideComment(match.range.first)) {
                    highlighted.addStyle(
                        SpanStyle(color = Color(0xFF6897BB)),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

        return TransformedText(
            highlighted.toAnnotatedString(),
            OffsetMapping.Identity
        )
    }
}
