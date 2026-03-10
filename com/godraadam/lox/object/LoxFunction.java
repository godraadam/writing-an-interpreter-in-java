package com.godraadam.lox.object;

import java.util.List;

import com.godraadam.lox.Interpreter;
import com.godraadam.lox.ast.Stmt;
import com.godraadam.lox.environment.Environment;

public class LoxFunction implements LoxCallable {

    private final Stmt.FuncDecl declaration;
    private final Environment closure;

    public LoxFunction(Stmt.FuncDecl declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i),
                    args.get(i));
        }

        return interpreter.execBlock(declaration.body.stmts, environment);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public boolean isVariadic() {
        return false;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

}
