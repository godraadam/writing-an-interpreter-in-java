package com.godraadam.lox;

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
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
    }

    private static void runRepl() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);

        System.out.println("Welcome to jlox version " + VERSION_STRING);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if ("exit".equals(line) || line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String input) {
        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public static void error(int line, String message) {
        System.err.println(
                "[line " + line + "] Error" + ": " + message);
        hadError = true;
    }
}