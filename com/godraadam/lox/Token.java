package com.godraadam.lox;

public class Token {

    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final Object literal;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.literal = literal;
    }

    @Override
    public String toString() {
        return type + " " + literal + " " + lexeme;
    }
}
