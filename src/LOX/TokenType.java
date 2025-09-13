package LOX;
/*
 * Enum stands for Enumeration
 * An enum is a special type of "class" that represents a group of constants
 * It can have attributes and methods just like a class but the enum constants are public, static and final
 * We use enums when we know that the values are not going to change
 * In our example, token type has specific values that don't change thus we use enum instead of classes
 */
public enum TokenType {
	//single-character tokens
	 LEFT_PAREN,      // (
	  RIGHT_PAREN,     // )
	  LEFT_BRACE,      // {
	  RIGHT_BRACE,     // }
	  COMMA,           // ,
	  DOT,             // .
	  MINUS,           // -
	  PLUS,            // +
	  SEMICOLON,       // ;
	  SLASH,           // /
	  STAR,            // *

	  // One or two character tokens.
	  BANG,            // !
	  BANG_EQUAL,      // !=
	  EQUAL,           // =
	  EQUAL_EQUAL,     // ==
	  GREATER,         // >
	  GREATER_EQUAL,   // >=
	  LESS,            // <
	  LESS_EQUAL,      // <=

	  // Literals
	  IDENTIFIER,      // variable names, function names, etc
	  STRING,          // string literals
	  NUMBER,          // number literals 
	  
	  // Keywords
	  AND, 
	  CLASS, 
	  ELSE, 
	  FALSE, 
	  FUN, 
	  FOR, 
	  IF, 
	  NIL, 
	  OR,
	  PRINT, 
	  RETURN, 
	  SUPER, 
	  THIS, 
	  TRUE, 
	  VAR, 
	  WHILE,
	  
	  //end of file - it is the last token added by the parse when the parsing process is over
	  EOF 
}
