package LOX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/*
 * Track variable declarations and definitions.
 * Enforce rules like "no reading a variable in its own initializer".
 * Tell the interpreter how far up the scope stack a variable lives (using resolveLocal).
 * Check for illegal use of return outside functions.
 * Set up scopes for functions, blocks, and conditionals.
 */

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private final Interpreter interpreter;

	// Stack of variableName : false (if only initialized) true (if fully defined)
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	private FunctionType currentFunction = FunctionType.NONE;

	private enum FunctionType {
		NONE, FUNCTION, METHOD, INITIALIZER
	}

	private enum ClassType {
		NONE, CLASS, SUBCLASS
	}

	private ClassType currentClass = ClassType.NONE;

	// constructor
	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	// begin a new scope, resolve all statements then terminate the scope
	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}

	// add the new scope to the stack
	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	// resolves each statement one by one
	void resolve(List<Stmt> statements) {
		for (Stmt statement : statements) {
			resolve(statement);
		}
	}

	private void resolve(Stmt statement) {
		statement.accept(this);
	}

	// first dispatch: used to determine the type of expresion
	// calls the corresponding visitor
	private void resolve(Expr expr) {
		expr.accept(this);
	}

	// removes the scope from the stack
	private void endScope() {
		scopes.pop();
	}

	// second dispatch
	// This prevents code like var a = a
	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		// mark as declared and not defined
		declare(stmt.name);
		// resolve RHS
		if (stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		// mark as fully defined
		define(stmt.name);
		return null;
	}

	// add to current scope as "declared but not defined"
	private void declare(Token name) {
		if (scopes.isEmpty())
			return;

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Bereits eine Variable mit diesem Namen in diesem Gültigkeitsbereich.");
		}
		scope.put(name.lexeme, false);
	}

	// mark as fully defined and usable
	private void define(Token name) {
		if (scopes.isEmpty())
			return;
		scopes.peek().put(name.lexeme, true);
	}

	// checks if the variable is being accessed before being defined
	// if it is defined, how far up the scope stack is it declared
	@Override
	public Void visitVariableExpr(Expr.Variable expr) {
		if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
			Lox.error(expr.name, "Kann lokale Variable in ihrer eigenen Initialisierung nicht lesen.");
		}

		resolveLocal(expr, expr.name);
		return null;
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size() - 1 - i);
				return;
			}
		}
	}

	@Override
	public Void visitAssignExpr(Expr.Assign expr) {
		// resolves the corresponding expression on the RHS
		resolve(expr.value);
		// figures out where the variable is
		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visitFunctionStmt(Stmt.Function stmt) {
		// declare and define a function name
		declare(stmt.name);
		define(stmt.name);
		// resolve the function
		resolveFunction(stmt, FunctionType.FUNCTION);
		return null;
	}

	private void resolveFunction(Stmt.Function function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;
		// new scope for the function
		beginScope();
		// add all parameters to the scope before resolving the body
		for (Token param : function.params) {
			declare(param);
			define(param);
		}
		// resolve the body
		resolve(function.body);
		// end the scope
		endScope();
		currentFunction = enclosingFunction;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		// resolves a top level expression
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		// resolves the condition and the branches of the if statement
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null)
			resolve(stmt.elseBranch);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		// resolve the expression to be printed
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		// resolves the return expression (if present)
		if (currentFunction == FunctionType.NONE) {
			Lox.error(stmt.keyword, "Kann nicht von Code auf oberster Ebene zurückgeben.");
		}
		if (stmt.value != null) {
			if (currentFunction == FunctionType.INITIALIZER) {
				Lox.error(stmt.keyword, "Kann keinen Wert von einem Initialisierung zurückgeben.");
			}

			resolve(stmt.value);
		}

		return null;
	}

	@Override
	public Void visitClassStmt(Stmt.Class stmt) {
		ClassType enclosingClass = currentClass;
		currentClass = ClassType.CLASS;
		declare(stmt.name);
		define(stmt.name);

		if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
			Lox.error(stmt.superclass.name, "Eine Klasse kann nicht von sich selbst erben.");
		}

		if (stmt.superclass != null) {
			currentClass = ClassType.SUBCLASS;
			resolve(stmt.superclass);
		}

		if (stmt.superclass != null) {
			beginScope();
			scopes.peek().put("super", true);
		}

		beginScope();
		scopes.peek().put("dies", true);

		for (Stmt.Function method : stmt.methods) {
			FunctionType declaration = FunctionType.METHOD;
			if (method.name.lexeme.equals("init")) {
				declaration = FunctionType.INITIALIZER;
			}
			resolveFunction(method, declaration);
		}

		endScope();

		if (stmt.superclass != null)
			endScope();
		currentClass = enclosingClass;
		return null;
	}

	@Override
	public Void visitSuperExpr(Expr.Super expr) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expr.keyword, "Kann 'super' nicht außerhalb einer Klasse verwenden.");
		} else if (currentClass != ClassType.SUBCLASS) {
			Lox.error(expr.keyword, "Kann 'super' nicht in einer Klasse ohne Oberklasse verwenden.");
		}

		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitThisExpr(Expr.This expr) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expr.keyword, "Kann 'dies' nicht außerhalb einer Klasse verwenden.");
			return null;
		}
		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		// resolves the condition and loop's body
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitBinaryExpr(Expr.Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitCallExpr(Expr.Call expr) {
		resolve(expr.callee);

		for (Expr argument : expr.arguments) {
			resolve(argument);
		}

		return null;
	}

	@Override
	public Void visitSetExpr(Expr.Set expr) {
		resolve(expr.value);
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitGetExpr(Expr.Get expr) {
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitGroupingExpr(Expr.Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visitLiteralExpr(Expr.Literal expr) {
		return null;
	}

	@Override
	public Void visitLogicalExpr(Expr.Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitUnaryExpr(Expr.Unary expr) {
		resolve(expr.right);
		return null;
	}
}