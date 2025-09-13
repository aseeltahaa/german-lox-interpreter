package LOX;

//Portions of this code are adapted from "Crafting Interpreters" by Bob Nystrom
//Available at: https://craftinginterpreters.com/
//Licensed under CC BY-NC-SA 4.0
//Modified by Aseel Taha, translated prompts and errors to German

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Benutzung: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        Path finalPath = Paths.get(path);
        byte[] bytes = Files.readAllBytes(finalPath);
        run(new String(bytes, Charset.forName("UTF-8"))); // ensures German characters are safe

        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in, Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if (hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        report(token.line, " bei '" + token.lexeme + "'", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[Zeile " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Zeile " + line + "] Fehler" + where + ": " + message);
        hadError = true;
    }
}
