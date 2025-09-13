package LOX;

// print Abstract Syntax Tree (AST) expressions in a parenthesized format
public class AstPrinter implements Expr.Visitor<String> {
	// main function
	public static void main(String[] args) {
		// testing
		Expr expression = new Expr.Binary(
				new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
				new Token(TokenType.STAR, "*", null, 1), new Expr.Grouping(new Expr.Literal(45.67)));

		System.out.println(new AstPrinter().print(expression));
	}

	// public interface method: first dispatch
	String print(Expr expr) {
		return expr.accept(this);
	}

	// helper method: creates a parenthesized representation of expressions.
	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();
		// opening parenthesis and the name
		builder.append("(").append(name);
		// recursively add each expression
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		// closing paranthesis
		builder.append(")");
		return builder.toString();
	}

	// visitors
	@Override
	public String visitAssignExpr(Expr.Assign expr) {
		return parenthesize("zuweisung " + expr.name.lexeme, expr.value);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitCallExpr(Expr.Call expr) {
		return parenthesize("aufruf", expr.callee);
	}

	@Override
	public String visitGetExpr(Expr.Get expr) {
		return parenthesize("holen " + expr.name.lexeme, expr.object);
	}

	@Override
	public String visitSetExpr(Expr.Set expr) {
		return parenthesize("setzen " + expr.name.lexeme, expr.object, expr.value);
	}

	@Override
	public String visitSuperExpr(Expr.Super expr) {
		return parenthesize("super " + expr.method.lexeme);
	}

	@Override
	public String visitThisExpr(Expr.This expr) {
		return "dies";
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("gruppe", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.value == null)
			return "nichts";
		return expr.value.toString();
	}

	@Override
	public String visitLogicalExpr(Expr.Logical expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.operator.lexeme, expr.right);
	}

	@Override
	public String visitVariableExpr(Expr.Variable expr) {
		return expr.name.lexeme;
	}
}