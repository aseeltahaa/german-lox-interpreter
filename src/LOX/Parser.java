package LOX;

import java.util.List;
import java.util.ArrayList;
import static LOX.TokenType.*;
import java.util.Arrays;

// Recursive Descent Parser: converts a list of tokens into an AST (Abstract Syntax Tree)
class Parser {
	// signal parse errors without crashing the program
	private static class ParseError extends RuntimeException {
	}

	private final List<Token> tokens;
	private int current = 0; // current token being processed

	// constructor
	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	// entry point
	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(declaration());
		}

		return statements;
	}

	// Recursive Descent Functions:

	// declaration -> funDecl | varDecl | statement | classDecl;
	private Stmt declaration() {
		try {
			if (match(FUN))
				return function("Funktion");
			if (match(CLASS))
				return classDeclaration();
			if (match(VAR))
				return varDeclaration();

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	// classDecl -> "klasse" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}"
	private Stmt classDeclaration() {
		// Identifier
		Token name = consume(IDENTIFIER, "Klassenname erwartet.");

		Expr.Variable superclass = null;
		if (match(LESS)) {
			consume(IDENTIFIER, "Name der Oberklasse erwartet.");
			superclass = new Expr.Variable(previous());
		}

		// {
		consume(LEFT_BRACE, "'{' vor Klassenkörper erwartet.");

		// functions
		List<Stmt.Function> methods = new ArrayList<>();
		while (!check(RIGHT_BRACE) && !isAtEnd()) {
			methods.add(function("Methode"));
		}

		// }
		consume(RIGHT_BRACE, "'}' nach Klassenkörper erwartet.");

		return new Stmt.Class(name, superclass, methods);
	}

	// funDecl → "funktion" function
	private Stmt.Function function(String kind) {
		// function → IDENTIFIER "(" parameters? ")" block
		Token name = consume(IDENTIFIER, kind + "sname erwartet.");
		consume(LEFT_PAREN, "'(' nach " + kind + "sname erwartet.");

		// parameters?
		List<Token> parameters = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (parameters.size() >= 255) {
					error(peek(), "Kann nicht mehr als 255 Parameter haben.");
				}
				parameters.add(consume(IDENTIFIER, "Parametername erwartet."));
			} while (match(COMMA));
		}

		consume(RIGHT_PAREN, "')' nach Parametern erwartet.");

		// block
		consume(LEFT_BRACE, "'{' vor " + kind + "skörper erwartet.");
		List<Stmt> body = block();

		return new Stmt.Function(name, parameters, body);
	}

	// varDeclaration -> "var" IDENTIFIER ( "=" expression )? ";"
	private Stmt varDeclaration() {
		Token name = consume(IDENTIFIER, "Variablenname erwartet.");

		Expr initializer = null;
		if (match(EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON, "';' nach Variablendeklaration erwartet.");
		return new Stmt.Var(name, initializer);
	}

	// statement → printStatement | ifStatement | expressionStatement | block |
	// returnStatement
	// matches each statement to its corresponding type
	private Stmt statement() {
		if (match(PRINT))
			return printStatement();
		if (match(IF))
			return ifStatement();
		if (match(LEFT_BRACE))
			return new Stmt.Block(block());
		if (match(WHILE))
			return whileStatement();
		if (match(FOR))
			return forStatement();
		if (match(RETURN))
			return returnStatement();
		return expressionStatement();

	}

	// returnStmt -> "zurückgeben" expression? ";"
	private Stmt returnStatement() {
		Token keyword = previous();
		Expr value = null;
		if (!check(SEMICOLON)) {
			value = expression();
		}

		consume(SEMICOLON, "';' nach Rückgabewert erwartet.");
		return new Stmt.Return(keyword, value);
	}

	private Stmt forStatement() {
		consume(LEFT_PAREN, "'(' nach 'für' erwartet.");

		// parse the initializer
		Stmt initializer;
		if (match(SEMICOLON)) {
			initializer = null;
		} else if (match(VAR)) {
			initializer = varDeclaration();
		} else {
			initializer = expressionStatement();
		}

		// parse the condition
		Expr condition = null;
		if (!check(SEMICOLON)) {
			condition = expression();
		}
		consume(SEMICOLON, "';' nach Schleifenbedingung erwartet.");

		// parse the increment
		Expr increment = null;
		if (!check(RIGHT_PAREN)) {
			increment = expression();
		}
		consume(RIGHT_PAREN, "')' nach für-Klauseln erwartet.");

		// parse the body of the loop (usually enclosed within a scope)
		Stmt body = statement();

		// if there is an increment, we break down the body into a List of: the body
		// followed by the increment
		if (increment != null) {
			body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
		}

		// Otherwise, no condition is added
		if (condition == null)
			condition = new Expr.Literal(true);
		body = new Stmt.While(condition, body);

		// if there is an initializer, wrap the while loop and the initializer
		if (initializer != null) {
			body = new Stmt.Block(Arrays.asList(initializer, body));
		}

		return body;
	}

	// whileStmt -> "während" "(" expression ")" statement ;
	private Stmt whileStatement() {
		consume(LEFT_PAREN, "'(' nach 'während' erwartet.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "')' nach Bedingung erwartet.");
		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	// ifStmt -> "wenn" "(" expression ")" statement ( "sonst" statement )? ;
	private Stmt ifStatement() {
		consume(LEFT_PAREN, "'(' nach 'wenn' erwartet.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "')' nach wenn-Bedingung erwartet.");
		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		// optional else block
		if (match(ELSE)) {
			elseBranch = statement();
		}
		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	// block -> "{" declaration* "}"
	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();

		while (!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "'}' nach Block erwartet.");
		return statements;
	}

	// parsing expression statements
	private Stmt expressionStatement() {
		Expr expr = expression();
		// ensure no syntax errors
		consume(SEMICOLON, "';' nach Ausdruck erwartet.");
		return new Stmt.Expression(expr);
	}

	// prints the statement
	private Stmt printStatement() {
		Expr value = expression();
		consume(SEMICOLON, "';' nach Wert erwartet.");
		return new Stmt.Print(value);
	}

	// expression -> assignment
	private Expr expression() {
		return assignment();
	}

	// assignment -> ( call "." )? IDENTIFIER "=" assignment | logic_or ;
	private Expr assignment() {
		// assume this is not an assignment, unless we can prove otherwise
		Expr expr = or();

		// if there is an equal, we are dealing with an assignment
		if (match(EQUAL)) {
			Token equals = previous();
			Expr value = assignment();
			// check if the left hand side is valid: it must be
			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable) expr).name;
				// return new node
				return new Expr.Assign(name, value);
			} else if (expr instanceof Expr.Get) {
				Expr.Get get = (Expr.Get) expr;
				return new Expr.Set(get.object, get.name, value);
			}
			// report error: complex/ invalid LHS
			error(equals, "Ungültiges Zuweisungsziel.");
		}
		return expr;
	}

	// logic_or -> logic_and ( "oder" logic_and )*
	private Expr or() {
		Expr expr = and();

		while (match(OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	// logic_and -> equality ( "und" equality )*
	private Expr and() {
		Expr expr = equality();

		while (match(AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	// equality -> comparison ( ( "!=" | "==" ) comparison )*
	private Expr equality() {
		Expr expr = comparison();

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	// comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
	private Expr comparison() {
		Expr expr = term();

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expr right = term();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	// term -> factor ( ( "-" | "+" ) factor )*
	private Expr term() {
		Expr expr = factor();

		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expr right = factor();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	// factor -> unary ( ( "/" | "*" ) unary )*
	private Expr factor() {
		Expr expr = unary();

		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	// unary -> ( "!" | "-" ) unary | call
	private Expr unary() {
		// if the current token is ! or -, we match the current expression to a unary
		// expression
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		// otherwise, we match it to a call
		return call();
	}

	// call -> primary ( "(" arguments? ")" | "." IDENTIFIER )*
	private Expr call() {
		// start with a primary expression (identifier, literal, etc.)
		Expr expr = primary();

		// handle chained function calls like callBack()()
		while (true) {
			// if there's a '(' then this is a function call
			if (match(LEFT_PAREN)) {
				expr = finishCall(expr);
			} else if (match(DOT)) {
				// it is a class field
				Token name = consume(IDENTIFIER, "Eigenschaftsname nach '.' erwartet.");
				expr = new Expr.Get(expr, name);
			} else {
				// no more function calls
				break;
			}
		}

		// return the fully parsed call expression
		return expr;
	}

	// parses the function arguments
	private Expr finishCall(Expr callee) {
		List<Expr> arguments = new ArrayList<>();
		// if the parentheses are not empty, parse the arguments
		if (!check(RIGHT_PAREN)) {
			do {
				if (arguments.size() >= 255) {
					error(peek(), "Kann nicht mehr als 255 Argumente haben.");
				}
				arguments.add(expression());
			} while (match(COMMA));
		}

		Token paren = consume(RIGHT_PAREN, "')' nach Argumenten erwartet.");

		// return a Call expression with the callee, the closing parenthesis, and the
		// argument list
		return new Expr.Call(callee, paren, arguments);
	}

	// primary -> NUMBER | STRING | "wahr" | "falsch" | "nichts" | "(" expression ")"
	private Expr primary() {
		if (match(FALSE))
			return new Expr.Literal(false);
		if (match(TRUE))
			return new Expr.Literal(true);
		if (match(NIL))
			return new Expr.Literal(null);

		if (match(NUMBER, STRING)) {
			return new Expr.Literal(previous().literal);
		}
		if (match(SUPER)) {
			Token keyword = previous();
			consume(DOT, "'.' nach 'super' erwartet.");
			Token method = consume(IDENTIFIER, "Name der Oberklassenmethode erwartet.");
			return new Expr.Super(keyword, method);
		}
		if (match(THIS))
			return new Expr.This(previous());

		if (match(IDENTIFIER)) {
			return new Expr.Variable(previous());
		}
		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "')' nach Ausdruck erwartet.");
			return new Expr.Grouping(expr);
		}

		throw error(peek(), "Ausdruck erwartet.");
	}

	// Utility methods for matching and advancing tokens

	// checks if the current token matches any of the given types; if it does, it is
	// consumed
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	// returns true if the current token is of the given type, without consuming it
	private boolean check(TokenType type) {
		if (isAtEnd())
			return false;
		return peek().type == type;
	}

	// moves to the next token and returns the previous one
	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	// returns true if we've reached the end of the token list
	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	// returns the current token without consuming it
	private Token peek() {
		return tokens.get(current);
	}

	// returns the most recently consumed token
	private Token previous() {
		return tokens.get(current - 1);
	}

	// ensures the current token matches the expected type. If so, it advances.
	// Otherwise, throws an error.
	private Token consume(TokenType type, String message) {
		if (check(type))
			return advance();
		throw error(peek(), message);
	}

	// error() reports a parsing error and returns a ParseError exception
	private ParseError error(Token token, String message) {
		Lox.error(token.line, message);
		return new ParseError();
	}

	// skips tokens until we're likely at the beginning of the next statement (used
	// after error recovery)
	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON)
				return;
			switch (peek().type) {
			case CLASS:
			case FUN:
			case VAR:
			case FOR:
			case IF:
			case WHILE:
			case PRINT:
			case RETURN:
				return;
			}

			advance();
		}
	}
}