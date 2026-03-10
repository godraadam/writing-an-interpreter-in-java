package com.godraadam.lox.object;

import java.util.List;

import com.godraadam.lox.Interpreter;

public interface LoxCallable {
    public Object call(Interpreter interpreter, List<Object> args);
    public int arity();
    public boolean isVariadic();
    
}