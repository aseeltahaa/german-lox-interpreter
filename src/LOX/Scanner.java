package LOX;

import static LOX.TokenType.*; // imports all TokenType constants for easier referencing
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	// pointers for tracking the position within the source
	private int start = 0; // start index of the current Lexeme being scanner
	private int current = 0; // current index
	private int line = 1; // line number: used for error reporting
	
	// reserved keywords - NOW IN GERMAN!
	private static final Map<String, TokenType> keywords;
	static {
		keywords = new HashMap<>();
		keywords.put("und", AND);           // "and" -> "und"
		keywords.put("klasse", CLASS);      // "class" -> "klasse"
		keywords.put("sonst", ELSE);        // "else" -> "sonst"
		keywords.put("falsch", FALSE);      // "false" -> "falsch"
		keywords.put("für", FOR);           // "for" -> "für"
		keywords.put("funktion", FUN);      // "fun" -> "funktion"
		keywords.put("wenn", IF);           // "if" -> "wenn"
		keywords.put("nichts", NIL);        // "nil" -> "nichts"
		keywords.put("oder", OR);           // "or" -> "oder"
		keywords.put("drucke", PRINT);      // "print" -> "drucke"
		keywords.put("zurückgeben", RETURN); // "return" -> "zurückgeben"
		keywords.put("super", SUPER);       // "super" stays "super"
		keywords.put("dies", THIS);         // "this" -> "dies"
		keywords.put("wahr", TRUE);         // "true" -> "wahr"
		keywords.put("var", VAR);           // "var" stays "var"
		keywords.put("während", WHILE);     // "while" -> "während"
	}

	// constructor
	Scanner(String source) {
		this.source = source;
	}

	// Main Loop
	List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current; // start of the token
			scanToken(); // scan the token
		}
		// at the end, add End Of File token
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	// used to detect strings
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || 
		       (c >= 'ä' && c <= 'ä') || (c >= 'ö' && c <= 'ö') || (c >= 'ü' && c <= 'ü') ||
		       (c >= 'Ä' && c <= 'Ä') || (c >= 'Ö' && c <= 'Ö') || (c >= 'Ü' && c <= 'Ü') ||
		       (c >= 'ß' && c <= 'ß');
	}

	// recognizes tokens and adds them to the list
	private void scanToken() {
		char c = advance();
		switch (c) {
		// single-character tokens
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '-':
			addToken(MINUS);
			break;
		case '+':
			addToken(PLUS);
			break;
		case ';':
			addToken(SEMICOLON);
			break;
		case '*':
			addToken(STAR);
			break;

		// tokens that could potentially be a single character or two characters
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);
			break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;

		// to handle comments
		case '/':
			if (match('/')) {
				// A comment goes until the end of the line
				while (peek() != '\n' && !isAtEnd())
					advance();
			} else {
				addToken(SLASH);
			}
			break;

		// ignore empty spaces
		case ' ': // empty space
		case '\r': // carriage return : In windows, a new line is denoted by \r\n
		case '\t': // tab character
			break; // Ignore white space

		// line feed
		case '\n':
			line++;
			break;

		// strings
		case '"':
			string();
			break;
		default:
			// numbers
			if (isDigit(c)) {
				number();
			}
			// reserved keywords
			else if (isAlpha(c)) {
				identifier();
			}
			// Syntax Error
			else {
				Lox.error(line, "Unerwartetes Zeichen.");
			}
			break;
		}

	}
	
	// used to distinguish between ==, <= , = , >=, etc.
	private boolean match(char expected) {
		if (isAtEnd())
			return false;
		if (source.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	// recognizes identifiers
	private void identifier() {
		while (isAlphaNumeric(peek()))
			advance();
		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null)
			type = IDENTIFIER;
		addToken(type);
	}

	// checks if a character is a digit or not
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	// recognizes numbers
	private void number() {
		while (isDigit(peek()))
			advance();
		// Look for a fractional part
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek()))
				advance();
		}
		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	// recognizes strings
	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n')
				line++;
			advance();
		}

		if (isAtEnd()) {
			Lox.error(line, "Unbeendete Zeichenkette.");
			return;
		}
		// the closing "
		advance();

		// trim the surrounding quotes.
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	// checks if we have reached the end of the source code
	private boolean isAtEnd() {
		return current >= source.length();
	}

	// returns the next character without consuming it
	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';
		return source.charAt(current + 1);
	}

	// move the pointer to the next character
	private char advance() {
		return source.charAt(current++);
	}

	// helper function: identifiers
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	// helper function
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	// adds the token into the tokens list
	private void addToken(TokenType type, Object literal) {
		//extract the token
		String text = source.substring(start, current);
		// add the new token to the list
		Token token = new Token(type, text, literal, line);
		tokens.add(token);
	}

	// lookahead: look up the character but do not consume it
	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}
}