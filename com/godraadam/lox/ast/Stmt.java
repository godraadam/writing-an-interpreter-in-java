package com.godraadam.lox.ast;

import java.util.List;

import com.godraadam.lox.token.Token;

public abstract class Stmt {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitExprStmt(Expression stmt);

        R visitVarDeclStmt(VarDecl stmt);

        R visitFuncDeclStmt(FuncDecl stmt);

        R visitBlockStmt(Block stmt);

        R visitIfStmt(If stmt);

        R visitWhileStmt(While stmt);

        R visitReturnStmt(Return stmt);
    }

    public static class Expression extends Stmt {
        public final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStmt(this);
        }
    }

    public static class VarDecl extends Stmt {
        public final Token name;
        public final Expr expr;

        public VarDecl(Token name, Expr expr) {
            this.name = name;
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclStmt(this);
        }
    }

    public static class FuncDecl extends Stmt {
        public final Token name;
        public final List<Token> params; // TODO: destructured params
        public final Block body;

        public FuncDecl(Token name, List<Token> params, Block body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFuncDeclStmt(this);
        }
    }

    public static class Block extends Stmt {
        public final List<Stmt> stmts;

        public Block(List<Stmt> stmts) {
            this.stmts = stmts;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBlock;
        public final Stmt elseBlock;

        public If(Expr condition, Stmt thenBlock, Stmt elseBlock) {
            this.condition = condition;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt block;

        public While(Expr condition, Stmt block) {
            this.condition = condition;
            this.block = block;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class Return extends Stmt {
        public final Expr value;
        public final Token token;

        public Return(Token token, Expr value) {
            this.value = value;
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }
}
