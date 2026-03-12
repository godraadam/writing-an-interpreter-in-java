package com.godraadam.lox.environment;

import java.util.HashMap;
import java.util.Map;

import com.godraadam.lox.exception.RuntimeError;
import com.godraadam.lox.token.Token;

public class Environment {
    private final Map<String, Object> environment = new HashMap<>();;
    private Environment parent;

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(Token name, Object value) {
        if (environment.containsKey(name.lexeme)) {
            throw new RuntimeError(name,
                    "Redefinition of variable '" + name.lexeme + "'.");
        }
        environment.put(name.lexeme, value);
    }

    public void defineGlobal(String name, Object value) {
        environment.put(name, value);
    }

    public Object get(Token name) {
        if (environment.containsKey(name.lexeme)) {
            return environment.get(name.lexeme);
        }
        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.parent;
        }

        return environment;
    }

    public Object getAt(int d, Token name) {
        return ancestor(d).get(name);
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).environment.put(name.lexeme, value);
    }

    public Object assign(Token name, Object value) {
        if (environment.containsKey(name.lexeme)) {
            return environment.put(name.lexeme, value);
        }
        if (parent != null) {
            return parent.assign(name, value);
        }
        throw new RuntimeError(name,
                "Assignment to undeclared variable '" + name.lexeme + "'.");
    }
}
