package com.godraadam.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.godraadam.lox.ast.Expr;
import com.godraadam.lox.ast.Expr.Assignment;
import com.godraadam.lox.ast.Expr.Binary;
import com.godraadam.lox.ast.Expr.Call;
import com.godraadam.lox.ast.Expr.Grouping;
import com.godraadam.lox.ast.Expr.Identifier;
import com.godraadam.lox.ast.Expr.Literal;
import com.godraadam.lox.ast.Expr.Logical;
import com.godraadam.lox.ast.Expr.Unary;
import com.godraadam.lox.ast.Stmt;
import com.godraadam.lox.ast.Stmt.Block;
import com.godraadam.lox.ast.Stmt.Expression;
import com.godraadam.lox.ast.Stmt.FuncDecl;
import com.godraadam.lox.ast.Stmt.If;
import com.godraadam.lox.ast.Stmt.Return;
import com.godraadam.lox.ast.Stmt.VarDecl;
import com.godraadam.lox.ast.Stmt.While;
import com.godraadam.lox.token.Token;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void beginScope() {
        this.scopes.push(new HashMap<>());
    }

    private void endScope() {
        this.scopes.pop();
    }

    public void resolveStmts(List<Stmt> stmts) {
        for (Stmt stmt : stmts) {
            resolveStmt(stmt);
        }
    }

    private void resolveStmt(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolveExpr(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.FuncDecl func) {
        beginScope();
        for (Token param : func.params) {
            declare(param);
            define(param);
        }
        resolveStmts(func.body.stmts);
        endScope();
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;
        Map<String, Boolean> scope = this.scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name,
                    "Already declared a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        this.scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - i - 1);
                return;
            }
        }
    }

    @Override
    public Void visitExprStmt(Expression stmt) {
        resolveExpr(stmt.expr);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(VarDecl stmt) {
        declare(stmt.name);
        if (stmt.expr != null) {
            resolveExpr(stmt.expr);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitFuncDeclStmt(FuncDecl stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt);
        return null;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        beginScope();
        resolveStmts(stmt.stmts);
        endScope();

        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolveExpr(stmt.condition);
        resolveStmt(stmt.thenBlock);
        if (stmt.elseBlock != null)
            resolveStmt(stmt.elseBlock);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolveExpr(stmt.condition);
        resolveStmt(stmt.block);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if (stmt.value != null)
            resolveExpr(stmt.value);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolveExpr(expr.left);
        resolveExpr(expr.right);
        return null;

    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolveExpr(expr);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolveExpr(expr.expr);
        return null;
    }

    @Override
    public Void visitIdentifierExpr(Identifier expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name,
                    "Can't read local variable in its own initializer.");

        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignmentExpr(Assignment expr) {
        resolveExpr(expr.rValue);
        resolveLocal(expr, expr.lValue);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        resolveExpr(expr.left);
        resolveExpr(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolveExpr(expr.callee);

        for (Expr arg : expr.args) {
            resolveExpr(arg);
        }
        return null;
    }

}
