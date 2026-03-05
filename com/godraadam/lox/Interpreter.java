package com.godraadam.lox;

import com.godraadam.lox.ast.Expr;
import com.godraadam.lox.ast.Expr.Binary;
import com.godraadam.lox.ast.Expr.Grouping;
import com.godraadam.lox.ast.Expr.Literal;
import com.godraadam.lox.ast.Expr.Unary;
import com.godraadam.lox.exception.RuntimeError;
import com.godraadam.lox.token.Token;
import com.godraadam.lox.token.TokenType;

public class Interpreter implements Expr.Visitor<Object> {

    public void interpret(Expr program) {
        try {
            Object value = eval(program);
            System.out.println(stringify(value));
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    private Object eval(Expr expr) {
        return expr.accept(this);
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;

        return left.equals(right);
    }

    private boolean truthy(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (boolean) value;
        if (value instanceof Number)
            return (double) value != 0;
        return true;
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
         if (object instanceof String) {
            return "\"" + object + "\"";
        }

        return object.toString();
    }

    private void checkNumberOperand(Object left, Token operator) {
        if (left instanceof Double)
            return;
        throw new RuntimeError(operator, "Expected number!");
    }

    private void checkNumberOperands(Object left, Token operator, Object right) {
        checkNumberOperand(left, operator);
        checkNumberOperand(right, operator);
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = eval(expr.left);
        Object right = eval(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case TokenType.MINUS:
                checkNumberOperands(left, expr.operator, right);
                return (double) left - (double) right;
            case TokenType.STAR:
                checkNumberOperands(left, expr.operator, right);
                return (double) left * (double) right;
            case TokenType.SLASH:
                checkNumberOperands(left, expr.operator, right);
                return (double) left / (double) right;

            case TokenType.GREATER:
                checkNumberOperands(left, expr.operator, right);
                return (double) left > (double) right;
            case TokenType.GREATER_EQUAL:
                checkNumberOperands(left, expr.operator, right);
                return (double) left >= (double) right;
            case TokenType.LESS:
                checkNumberOperands(left, expr.operator, right);
                return (double) left < (double) right;
            case TokenType.LESS_EQUAL:
                checkNumberOperands(left, expr.operator, right);
                return (double) left <= (double) right;

            case TokenType.BANG_EQUAL:
                return !isEqual(left, right);
            case TokenType.EQUAL_EQUAL:
                return isEqual(left, right);

            default:
                return null;
        }
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object operand = eval(expr.operand);
        System.out.println(expr.operator.type);
        switch (expr.operator.type) {
            case TokenType.BANG:
                return !truthy(operand);
            case TokenType.MINUS:
                checkNumberOperand(operand, expr.operator);
                return -((double) operand);
            default:
                return null;
        }
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return eval(expr.expr);
    }

}
