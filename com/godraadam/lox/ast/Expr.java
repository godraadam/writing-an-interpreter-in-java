package com.godraadam.lox.ast;

import java.util.List;

import com.godraadam.lox.token.Token;

public abstract class Expr {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitUnaryExpr(Unary expr);

        R visitLiteralExpr(Literal expr);

        R visitGroupingExpr(Grouping expr);

        R visitIdentifierExpr(Identifier expr);

        R visitAssignmentExpr(Assignment expr);

        R visitLogicalExpr(Logical expr);

        R visitCallExpr(Call expr);
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Unary extends Expr {
        public final Expr operand;
        public final Token operator;

        public Unary(Expr operand, Token operator) {
            this.operand = operand;
            this.operator = operator;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expr;

        public Grouping(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Identifier extends Expr {
        public final Token name;

        public Identifier(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIdentifierExpr(this);
        }
    }

    public static class Assignment extends Expr {
        public final Token lValue;
        public final Expr rValue;

        public Assignment(Token lValue, Expr rValue) {
            this.lValue = lValue;
            this.rValue = rValue;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignmentExpr(this);
        }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final Expr right;
        public final Token operator;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final List<Expr> args;
        public final Token token;

        public Call(Expr callee, Token token, List<Expr> args) {
            this.callee = callee;
            this.args = args;
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }
}
