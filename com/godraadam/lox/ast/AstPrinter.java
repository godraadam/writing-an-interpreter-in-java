package com.godraadam.lox.ast;

import com.godraadam.lox.ast.Expr.Grouping;
import com.godraadam.lox.ast.Expr.Literal;
import com.godraadam.lox.ast.Expr.Unary;

public class AstPrinter implements Expr.Visitor<String> {
  public String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme,
        expr.left, expr.right);
  }

  @Override
  public String visitLiteralExpr(Literal expr) {
    if (expr.value == null)
      return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.operand);
  }

  @Override
  public String visitGroupingExpr(Grouping expr) {
    return parenthesize("grouping", expr.expr);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

}