package com.godraadam.lox;

import com.godraadam.lox.ast.Expr;
import com.godraadam.lox.ast.Stmt;
import com.godraadam.lox.environment.Environment;
import com.godraadam.lox.exception.RuntimeError;
import com.godraadam.lox.token.Token;
import com.godraadam.lox.token.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    final static String VERSION_STRING = "0.0.1";
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: lox [script]");
            System.exit(64);
        }
        if (args.length == 1) {
            runFile(args[0]);
        } else {
            runRepl();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()), new Environment());

        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runRepl() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);

        System.out.println("Welcome to jlox version " + VERSION_STRING);

        // we can pass in env vars here
        Environment env = new Environment();
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if ("exit".equals(line) || line == null)
                break;
            run(line, env);
            hadError = false;
        }
    }

    private static void run(String input, Environment env) {
        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        if (hadError) {
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> program = parser.parse();

        if (hadError) {
            return;
        }

        Interpreter interpreter = new Interpreter(env);
        interpreter.interpret(program);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}