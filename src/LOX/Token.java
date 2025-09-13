package LOX;

public class Token {
	final TokenType type; 
	final String lexeme; 
	final Object literal; 
	final int line; //in case of error, we will get to know at which line the error occured
	
	// constructor
	Token(TokenType type, String lexeme, Object literal, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}
	
	// to string (for testing)
	public String toString() {
		return type + " " + lexeme + " " + literal;
	}
}
