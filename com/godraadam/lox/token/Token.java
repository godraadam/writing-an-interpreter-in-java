package com.godraadam.lox.token;

public class Token {

    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final Object literal;

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
