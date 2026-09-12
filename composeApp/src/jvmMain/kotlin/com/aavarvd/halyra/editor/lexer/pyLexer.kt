package com.aavarvd.halyra.editor.lexer

class PyLexer {
    private val keywords = setOf(
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
        "yield"
    )

    private val builtins = setOf(
        "print",
        "input",
        "len",
        "range",
        "sum",
        "min",
        "max",
        "abs",
        "open",
        "type",
        "int",
        "float",
        "str",
        "list",
        "dict",
        "set"
    )

    private val operators = listOf(
        "**=", "//=",

        "==", "!=", "<=", ">=",

        "+=", "-=", "*=", "/=",
        "%=",

        "**", "//",

        "<<", ">>",

        "&=", "|=", "^=",

        ":=",

        "+", "-", "*", "/",
        "%", "=", "<", ">",
        "&", "|", "^", "~"
    )

    fun tokenize(code: String): List<Token> {
        val tokens = mutableListOf<Token>()

        var i = 0
        var iterations = 0
        while (i < code.length) {

            iterations++
            if (iterations % 5000 == 0) println("iterations=$iterations i=$i len=${code.length}")
            val c = code[i]

            val matchedOperator = operators.firstOrNull {
                code.startsWith(it, i)
            }

            if (matchedOperator != null) {
                tokens += Token(
                    TokenType.OPERATOR,
                    i,
                    i + matchedOperator.length,
                    matchedOperator
                )

                i += matchedOperator.length
                continue
            }

            when {

                // String
                c == '"' || c == '\'' -> {

                    val start = i
                    val quote = c

                    i++

                    while (i < code.length && code[i] != quote) {
                        i++
                    }

                    if (i < code.length) {
                        i++
                    }

                    tokens += Token(
                        TokenType.STRING,
                        start,
                        i,
                        code.substring(start, i)
                    )
                }

                c == '#' -> {

                    val start = i

                    while (i < code.length && code[i] != '\n') {
                        i++
                    }

                    tokens += Token(
                        TokenType.COMMENT,
                        start,
                        i,
                        code.substring(start, i)
                    )
                }

                c.isLetter() || c == '_' -> {
                    val start = i

                    while (i < code.length && (code[i].isLetterOrDigit() || code[i] == '_')) i++

                    val text = code.substring(start, i)

                    tokens += Token(
                        when {
                            text in keywords -> TokenType.KEYWORD
                            text in builtins -> TokenType.BUILTIN
                            else -> TokenType.IDENTIFIER
                        },
                        start,
                        i,
                        text
                    )
                }

                c.isDigit() -> {
                    val start = i

                    while (i < code.length && code[i].isDigit()) i++

                    if (i < code.length && code[i] == '.') {
                        i++
                        while (i < code.length && code[i].isDigit()) i++
                    }

                    tokens += Token(
                        TokenType.NUMBER,
                        start,
                        i,
                        code.substring(start, i)
                    )
                }

                c == ' ' || c == '\t' -> {
                    val start = i

                    while (i < code.length && (code[i] == ' ' || code[i] == '\t')) {
                        i++
                    }

                    tokens += Token(
                        TokenType.WHITESPACE,
                        start,
                        i,
                        code.substring(start, i)
                    )
                }

                c == '\n' -> {
                    tokens += Token(
                        TokenType.NEWLINE,
                        i,
                        i + 1,
                        "\n"
                    )
                    i++
                }

                c == '(' -> {
                    tokens += Token(
                        TokenType.LPAREN,
                        i,
                        i + 1,
                        "("
                    )
                    i++
                }

                c == ')' -> {
                    tokens += Token(
                        TokenType.RPAREN,
                        i,
                        i + 1,
                        ")"
                    )
                    i++
                }

                c == '[' -> {
                    tokens += Token(
                        TokenType.LBRACKET,
                        i,
                        i + 1,
                        "["
                    )
                    i++
                }

                c == ']' -> {
                    tokens += Token(
                        TokenType.RBRACKET,
                        i,
                        i + 1,
                        "]"
                    )
                    i++
                }

                c == '{' -> {
                    tokens += Token(
                        TokenType.LBRACE,
                        i,
                        i + 1,
                        "{"
                    )
                    i++
                }

                c == '}' -> {
                    tokens += Token(
                        TokenType.RBRACE,
                        i,
                        i + 1,
                        "}"
                    )
                    i++
                }

                c == ':' -> {
                    tokens += Token(
                        TokenType.COLON,
                        i,
                        i + 1,
                        ":"
                    )
                    i++
                }

                c == ',' -> {
                    tokens += Token(
                        TokenType.COMMA,
                        i,
                        i + 1,
                        ","
                    )
                    i++
                }

                c == '.' -> {
                    tokens += Token(
                        TokenType.DOT,
                        i,
                        i + 1,
                        "."
                    )
                    i++
                }

                else -> {
                    tokens += Token(
                        type = TokenType.UNKNOWN,
                        start = i,
                        end = i + 1,
                        text = c.toString()
                    )

                    i++
                }
            }
        }
        println("PyLexer: done, iterations=$iterations for ${code.length} chars")
        return tokens
    }
}

fun main() {
    val lexer = PyLexer()

//    val tokens = lexer.tokenize(
//        """
//        def hello(name):
//            print("Hello")
//            score = 42
//            pi = 3.14159265358979
//        """.trimIndent()
//    )
    val tokens = lexer.tokenize(
        """
    # This is a comment
    print("Hello") # greeting
    print("# not a comment")
    """.trimIndent()
    )

    tokens.forEach(::println)
}