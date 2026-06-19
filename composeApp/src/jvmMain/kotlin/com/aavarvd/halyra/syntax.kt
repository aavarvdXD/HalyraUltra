package com.aavarvd.halyra

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import kotlin.code

class PythonHighLightTransformation : VisualTransformation {
    private val keywords = listOf(
        // Keywords
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

        // Built-in types
        "int", "float", "str", "bool",
        "list", "tuple", "dict", "set",
        "bytes", "bytearray", "complex",
        "frozenset", "object", "type",

        // Common built-ins
        "print", "input", "len", "range",
        "enumerate", "zip", "map", "filter",
        "sum", "min", "max", "abs",
        "round", "sorted", "reversed",
        "open", "isinstance", "issubclass",
        "super", "property",
        "staticmethod", "classmethod",

        // Exceptions
        "Exception", "ValueError",
        "TypeError", "KeyError",
        "IndexError", "AttributeError",
        "RuntimeError", "ImportError",
        "ModuleNotFoundError",
        "FileNotFoundError",
        "ZeroDivisionError",

        // Common modules
        "os", "sys", "math", "random",
        "time", "datetime",
        "json", "re", "pathlib",
        "collections", "itertools",
        "functools", "typing",
        "threading", "asyncio",

        // Constants
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

        fun isInsideString(index : Int): Boolean {
            return stringRanges.any() { index in it }
        }

        fun isInsideComment(index : Int): Boolean {
            return commentRanges.any() { index in it }
        }

        Regex("#.*")
            .findAll(code)
            .forEach {
                highlighted.addStyle(
                    SpanStyle(
                        color = Color(0xFF808080)
                    ),
                    it.range.first,
                    it.range.last + 1
                )
            }

        Regex("\".*?\"|'.*?'")
            .findAll(code)
            .forEach {
                highlighted.addStyle(
                    SpanStyle(
                        color = Color(0xFF6A8759)
                    ),
                    it.range.first,
                    it.range.last + 1
                )
            }

        keywords.forEach { keyword ->
            Regex("\\b$keyword\\b")
                .findAll(code)
                .forEach { match ->
                    if (
                        !isInsideString(match.range.first)
                        && !isInsideComment(match.range.first)
                    ) {
                        highlighted.addStyle(
                            SpanStyle(
                                color = Color(0xFFCC7832)
                            ),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                }
        }

            Regex("\\b\\d+(\\.\\d+)?\\b")
                .findAll(code)
                .forEach { match ->
                    if (
                        !isInsideString(match.range.first)
                        && !isInsideComment(match.range.first)
                    ) {
                        highlighted.addStyle(
                            SpanStyle(
                                color = Color(0xFF6897BB)
                            ),
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