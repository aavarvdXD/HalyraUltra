package com.aavarvd.halyra.editor.lexer

class PyTokenAnalyzer {
    fun analyze(tokens: List<Token>): List<Token> {

        val result = tokens.toMutableList()

        for (i in tokens.indices) {

            val token = tokens[i]

            val previous = previousNonWhitespace(tokens, i)
            val next = nextNonWhitespace(tokens, i)

            if (
                token.type == TokenType.IDENTIFIER &&
                previous?.text == "def"
            ) {
                result[i] = token.copy(
                    type = TokenType.FUNCTION_NAME
                )
                continue
            }

            if (
                token.type == TokenType.IDENTIFIER &&
                previous?.text == "class"
            ) {
                result[i] = token.copy(
                    type = TokenType.CLASS_NAME
                )
                continue
            }

            if (
                token.type == TokenType.IDENTIFIER &&
                next?.type == TokenType.LPAREN
            ) {
                result[i] = token.copy(
                    type = TokenType.FUNCTION_CALL
                )
            }
        }

        return result
    }

    private fun previousNonWhitespace(
        tokens: List<Token>,
        index: Int
    ): Token? {

        var i = index - 1

        while (i >= 0) {

            if (
                tokens[i].type != TokenType.WHITESPACE &&
                tokens[i].type != TokenType.NEWLINE
            ) {
                return tokens[i]
            }

            i--
        }

        return null
    }

    private fun nextNonWhitespace(
        tokens: List<Token>,
        index: Int
    ): Token? {

        var i = index + 1

        while (i < tokens.size) {

            if (
                tokens[i].type != TokenType.WHITESPACE &&
                tokens[i].type != TokenType.NEWLINE
            ) {
                return tokens[i]
            }

            i++
        }

        return null
    }
}