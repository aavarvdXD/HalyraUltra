package com.aavarvd.halyra.editor.lexer

enum class TokenType {
    KEYWORD,
    IDENTIFIER,
    BUILTIN,

    STRING,
    NUMBER,
    COMMENT,

    OPERATOR,

    LPAREN,
    RPAREN,

    LBRACKET,
    RBRACKET,

    LBRACE,
    RBRACE,

    COMMA,
    COLON,
    DOT,

    WHITESPACE,
    NEWLINE,

    UNKNOWN
}

data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int,
    val text: String
)