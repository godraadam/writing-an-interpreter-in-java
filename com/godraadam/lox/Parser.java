package com.godraadam.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// import com.godraadam.lox.ast.AstPrinter;
import com.godraadam.lox.ast.Expr;
import com.godraadam.lox.ast.Stmt;
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

    public List<Stmt> parse() {
        List<Stmt> program = new ArrayList<>();
        while (!isAtEnd()) {
            program.add(parseStmt());
        }
        return program;
    }

    private Stmt parseStmt() {
        try {
            if (match(TokenType.LET)) {
                return parseVarDecl();
            }
            if (match(TokenType.LEFT_BRACE)) {
                return parseBlock();
            }
            if (match(TokenType.IF)) {
                return parseIf();
            }
            if (match(TokenType.FOR)) {
                return parseFor();
            }
            if (match(TokenType.WHILE)) {
                return parseWhile();
            }
            if (match(TokenType.FN)) {
                return parseFuncDecl();
            }
            if (match(TokenType.RETURN)) {
                return parseReturn();
            }
            return parseExprStmt();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt parseExprStmt() {
        Expr expr = parseExpr();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt parseVarDecl() {
        Token name = consume(TokenType.IDENTIFIER, "Expected identifier");

        Expr value = null;
        if (match(TokenType.EQUAL)) {
            value = parseExpr();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.VarDecl(name, value);
    }

    private Expr parseExpr() {
        return parseAssignment();
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

        return parseCall();
    }

    private Expr parsePrimary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);
        if (match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(prev().literal);
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Identifier(prev());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = parseExpr();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Expr parseOr() {
        Expr left = parseAnd();

        while (match(TokenType.OR)) {
            Token operator = prev();
            Expr right = parseAnd();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr parseAnd() {
        Expr left = parseEquality();

        while (match(TokenType.AND)) {
            Token operator = prev();
            Expr right = parseEquality();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr parseAssignment() {
        Expr lValue = parseOr();

        if (match(TokenType.EQUAL)) {
            Token equals = prev();
            Expr rValue = parseAssignment();

            if (lValue instanceof Expr.Identifier) {
                Token name = ((Expr.Identifier) lValue).name;
                return new Expr.Assignment(name, rValue);
            }
            error(equals, "Invalid assignment target.");
        }
        return lValue;
    }

    private Expr parseCall() {
        Expr expr = parsePrimary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCallExpr(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCallExpr(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(parseExpr());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Stmt parseBlock() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(parseStmt());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return new Stmt.Block(statements);
    }

    private Stmt parseIf() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'");
        Expr condition = parseExpr();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition");
        Stmt thenBlock = parseStmt();

        Stmt elseBlock = null;
        if (match(TokenType.ELSE)) {
            elseBlock = parseStmt();
        }
        return new Stmt.If(condition, thenBlock, elseBlock);

    }

    private Stmt parseWhile() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
        Expr condition = parseExpr();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition");
        Stmt block = parseStmt();

        return new Stmt.While(condition, block);
    }

    private Stmt parseFor() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'");
        Stmt initializer = null;
        if (match(TokenType.SEMICOLON)) {
            // do nothing
        } else if (match(TokenType.LET)) {
            initializer = parseVarDecl();
        } else {
            initializer = parseExprStmt();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpr();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after for condition");

        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = parseExpr();
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses");
        Stmt body = parseStmt();

        if (increment != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)));
        }

        if (condition == null)
            condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt parseFuncDecl() {
        Token name = consume(TokenType.IDENTIFIER, "Expect function name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");

        List<Token> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                params.add(
                        consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");

        Stmt body = parseBlock();

        return new Stmt.FuncDecl(name, params, (Stmt.Block) body);
    }

    private Stmt parseReturn() {
        Token token = prev();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpr();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.");

        return new Stmt.Return(token, value);
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
                case TokenType.RETURN:
                    return;
                default:
                    break;
            }

            advance();
        }
    }
}
