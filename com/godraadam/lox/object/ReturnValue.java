package com.godraadam.lox.object;

public class ReturnValue {
    Object value;

    public ReturnValue(Object value) {
        this.value = value;
    }

    public Object unwrap() {
        return this.value;
    }
}
