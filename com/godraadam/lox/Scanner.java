package com.godraadam.lox;

import com.godraadam.lox.token.Token;
import com.godraadam.lox.token.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    private final String input;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private final static Map<String, TokenType> keyWordMap;

    static {
        keyWordMap = new HashMap<>();
        keyWordMap.put("class", TokenType.CLASS);
        keyWordMap.put("else", TokenType.ELSE);
        keyWordMap.put("false", TokenType.FALSE);
        keyWordMap.put("for", TokenType.FOR);
        keyWordMap.put("fn", TokenType.FN);
        keyWordMap.put("if", TokenType.IF);
        keyWordMap.put("nil", TokenType.NIL);
        keyWordMap.put("return", TokenType.RETURN);
        keyWordMap.put("super", TokenType.SUPER);
        keyWordMap.put("this", TokenType.THIS);
        keyWordMap.put("true", TokenType.TRUE);
        keyWordMap.put("let", TokenType.LET);
        keyWordMap.put("while", TokenType.WHILE);
    }

    public Scanner(String input) {
        this.input = input;
    }

    public List<Token> scanTokens() {

        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= input.length();
    }

    private char advance() {
        return input.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (input.charAt(current) != expected)
            return false;
        current += 1;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return input.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= input.length())
            return '\0';
        return input.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9') || c == '_';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void readString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line += 1;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string literal!");
            return;
        }
        advance();
        String value = input.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void readNumber() {
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek()))
                advance();
        }

        addToken(TokenType.NUMBER, Double.valueOf(currentLexeme().replace("_", "")));
    }

    private void readIdentifier() {
        while (isAlphaNum(peek()))
            advance();

        TokenType type = keyWordMap.get(currentLexeme());
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private String currentLexeme() {
        return input.substring(start, current);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, currentLexeme(), literal, line));
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '[' -> addToken(TokenType.LEFT_BRACKET);
            case ']' -> addToken(TokenType.RIGHT_BRACKET);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '"' -> readString();
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '|' -> {
                if (match('|'))
                    addToken(TokenType.OR);
                else
                    Lox.error(line, "Unexpected input: '|'");
            }
            case '&' -> {
                if (match('&'))
                    addToken(TokenType.AND);
                else
                    Lox.error(line, "Unexpected input: '&'");
            }
            case ' ', '\t', '\r' -> {
                // skip wihtespace
            }
            case '\n' -> line += 1;
            default -> {
                if (isDigit(c)) {
                    readNumber();
                } else if (isAlpha(c)) {
                    readIdentifier();
                } else {
                    Lox.error(line, "Unexpected input!");
                }
            }
        }
    }

}