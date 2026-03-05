package com.godraadam.lox;

import java.util.List;

import com.godraadam.lox.ast.Expr;
import com.godraadam.lox.token.Token;
import com.godraadam.lox.token.TokenType;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token prev() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return tokens.get(current).type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current += 1;
        return prev();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);

    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    public Expr parse() {
        try {
            return parseExpr();
        } catch (ParseError e) {
            return null;
        }
    }

    private Expr parseExpr() {
        return parseEquality();
    }

    private Expr parseEquality() {
        Expr expr = parseComparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token op = prev();
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr parseComparison() {
        Expr expr = parseTerm();

        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token op = prev();
            Expr right = parseTerm();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr parseTerm() {
        Expr expr = parseFactor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token op = prev();
            Expr right = parseFactor();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr parseFactor() {
        Expr expr = parseUnary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token op = prev();
            Expr right = parseUnary();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr parseUnary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = prev();
            Expr operand = parseUnary();
            return new Expr.Unary(operand, operator);
        }

        return parsePrimary();
    }

    private Expr parsePrimary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(prev().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = parseExpr();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    // after encountering a parse error skip ahead to next probable statement
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (prev().type == TokenType.SEMICOLON) {
                return;
            }
            switch (peek().type) {
                case TokenType.CLASS:
                case TokenType.FN:
                case TokenType.LET:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.PRINT:
                case TokenType.RETURN:
                    return;
                default:
                    break;
            }

            advance();
        }
    }
}
