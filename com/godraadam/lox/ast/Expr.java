package com.godraadam.lox.ast;

import com.godraadam.lox.token.Token;

public abstract class Expr {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitUnaryExpr(Unary expr);

        R visitLiteralExpr(Literal expr);

        R visitGroupingExpr(Grouping expr);
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
}
