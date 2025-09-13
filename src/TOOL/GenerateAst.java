package TOOL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

//automatically generates Java source code for the AST node classes used in the interpreter. 
public class GenerateAst {
	// takes the output directory as an argument.
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		String outputDir = args[0];

		// ClassName : fieldType fieldName, ...
		// Expressions
		defineAst(outputDir, "Expr",
				Arrays.asList(
						"Call     : Expr callee, Token paren, List<Expr> arguments",
						"Get      : Expr object, Token name",
						"Set      : Expr object, Token name, Expr value",
						"Super    : Token keyword, Token method",
						"This     : Token keyword",
						"Assign   : Token name, Expr value", 
						"Binary   : Expr left, Token operator, Expr right",
						"Grouping : Expr expression", "Literal  : Object value",
						"Logical  : Expr left, Token operator, Expr right", 
						"Unary    : Token operator, Expr right",
						"Variable : Token name"));

				defineAst(outputDir, "Stmt", Arrays.asList(
					    "Block      : List<Stmt> statements", 
					    "Class      : Token name, Expr.Variable superclass," + " List<Stmt.Function> methods",
					    "Expression : Expr expression",
					    "Function   : Token name, List<Token> params, List<Stmt> body",
					    "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
					    "Print      : Expr expression",
					    "Var        : Token name, Expr initializer", 
					    "While      : Expr condition, Stmt body",
					    "Return     : Token keyword, Expr value"
					));
	}
	private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
		// writes to a file named Expr.java in the given directory.
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");

		// add the package
		writer.println("package LOX;");
		writer.println();
		// import libraries
		writer.println("import java.util.List;");
		writer.println();
		// declare an abstract class
		writer.println("abstract class " + baseName + " {");

		// define visitor
		defineVisitor(writer, baseName, types);

		// adds the subclass and its fields
		for (String type : types) {
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, className, fields);
		}

		writer.println();
		writer.println("  abstract <R> R accept(Visitor<R> visitor);");

		// end of base class
		writer.println("}");
		writer.close();
	}

	// helper function: creates the subclass
	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		// declare a class
		writer.println("  static class " + className + " extends " + baseName + " {");

		// constructor start
		writer.println("    " + className + "(" + fieldList + ") {");

		// stores the fields
		String[] fields = fieldList.split(", ");
		for (String field : fields) {
			String name = field.split(" ")[1];
			writer.println("      this." + name + " = " + name + ";");
		}

		// constructor end
		writer.println("    }");

		// Visitor pattern.
		writer.println();
		writer.println("    @Override");
		writer.println("    <R> R accept(Visitor<R> visitor) {");
		writer.println("      return visitor.visit" + className + baseName + "(this);");
		writer.println("    }");

		// adding the fields
		writer.println();
		for (String field : fields) {
			writer.println("    final " + field + ";");
		}
		// end of class
		writer.println("  }");
	}

	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("  interface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
		}

		writer.println("  }");
	}

}
