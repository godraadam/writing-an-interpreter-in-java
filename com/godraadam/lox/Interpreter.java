package com.godraadam.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.godraadam.lox.ast.Expr;
import com.godraadam.lox.ast.Stmt;
import com.godraadam.lox.ast.Stmt.Block;
import com.godraadam.lox.ast.Stmt.Expression;
import com.godraadam.lox.ast.Stmt.FuncDecl;
import com.godraadam.lox.ast.Stmt.If;
import com.godraadam.lox.ast.Stmt.Return;
import com.godraadam.lox.ast.Stmt.VarDecl;
import com.godraadam.lox.ast.Stmt.While;
import com.godraadam.lox.environment.Environment;
import com.godraadam.lox.ast.Expr.Assignment;
import com.godraadam.lox.ast.Expr.Binary;
import com.godraadam.lox.ast.Expr.Call;
import com.godraadam.lox.ast.Expr.Grouping;
import com.godraadam.lox.ast.Expr.Identifier;
import com.godraadam.lox.ast.Expr.Literal;
import com.godraadam.lox.ast.Expr.Logical;
import com.godraadam.lox.ast.Expr.Unary;
import com.godraadam.lox.exception.RuntimeError;
import com.godraadam.lox.object.LoxCallable;
import com.godraadam.lox.object.LoxFunction;
import com.godraadam.lox.object.ReturnValue;
import com.godraadam.lox.token.Token;
import com.godraadam.lox.token.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    public final Environment globals;
    private Environment environment;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter(Environment env) {
        globals = env;
        environment = globals;
        globals.defineGlobal("print", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                List<String> strings = new ArrayList<>();
                for (Object arg : args) {
                    strings.add(stringify(arg));
                }
                System.out.println(String.join(", ", strings));
                return null;
            }

            @Override
            public boolean isVariadic() {
                return true;
            }

            @Override
            public String toString() {
                return "<native fn print>";
            }
        });

        globals.defineGlobal("now", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter,
                    List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public boolean isVariadic() {
                return false;
            }

            @Override
            public String toString() {
                return "<native fn now>";
            }
        });
    }

    public void interpret(List<Stmt> program) {
        try {
            for (Stmt stmt : program) {
                exec(stmt);
            }
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    private Object eval(Expr expr) {
        return expr.accept(this);
    }

    private Object exec(Stmt stmt) {
        return stmt.accept(this);
    }

    public Object execBlock(List<Stmt> statements,
            Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                Object rv = exec(statement);
                if (rv instanceof ReturnValue) {
                    this.environment = previous;
                    return rv;
                }
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    public void resolve(Expr expr, int d) {
        locals.put(expr, d);
    }

    public Object lookupVar(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name);
        }
        return environment.get(name);
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

    @Override
    public Object visitIdentifierExpr(Identifier expr) {
        return lookupVar(expr.name, expr);
    }

    @Override
    public Object visitExprStmt(Expression stmt) {
        return eval(stmt.expr);
    }

    @Override
    public Object visitVarDeclStmt(VarDecl stmt) {
        Object value = null;
        if (stmt.expr != null) {
            value = eval(stmt.expr);
        }
        environment.define(stmt.name, value);
        return null;
    }

    @Override
    public Object visitAssignmentExpr(Assignment expr) {
        Object value = eval(expr.rValue);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.lValue, value);
        } else {
            globals.assign(expr.lValue, value);
        }
        return null;
    }

    @Override
    public Object visitBlockStmt(Block block) {
        Environment currentEnv = environment;
        Environment blockEnv = new Environment(currentEnv);
        return execBlock(block.stmts, blockEnv);
    }

    @Override
    public Object visitIfStmt(If stmt) {
        boolean condition = truthy(eval(stmt.condition));

        if (condition) {
            return exec(stmt.thenBlock);
        } else if (stmt.elseBlock != null) {
            return exec(stmt.elseBlock);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = eval(expr.left);

        if (expr.operator.type == TokenType.AND) {
            if (!truthy(left))
                return left;
        } else {
            if (truthy(left))
                return left;
        }

        return eval(expr.right);
    }

    @Override
    public Object visitWhileStmt(While stmt) {
        while (truthy(eval(stmt.condition))) {
            Object rv = exec(stmt.block);
            if (rv instanceof ReturnValue) {
                return rv;
            }
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Call expr) {
        Object callee = eval(expr.callee);

        List<Object> args = new ArrayList<>();

        for (Expr arg : expr.args) {
            args.add(eval(arg));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.token,
                    "Can only call functions and classes.");
        }

        LoxCallable func = (LoxCallable) callee;

        if (!func.isVariadic() && args.size() != func.arity()) {
            throw new RuntimeError(expr.token, "Expected " +
                    func.arity() + " arguments but got " +
                    args.size() + ".");
        }
        Object rv = func.call(this, args);
        if (rv instanceof ReturnValue) {
            return ((ReturnValue) rv).unwrap();
        }
        return rv;
    }

    @Override
    public Object visitFuncDeclStmt(FuncDecl stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name, function);
        return null;
    }

    @Override
    public Object visitReturnStmt(Return stmt) {
        return new ReturnValue(eval(stmt.value));
    }
}
