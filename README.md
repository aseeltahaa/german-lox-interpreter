# German Lox Interpreter

A German localized version of the Lox programming language interpreter, based on Bob Nystrom's "Crafting Interpreters" book. This implementation translates all keywords, error messages, and user-facing content to German while maintaining full compatibility with the original Lox language features.

## Features

- **German Keywords**: All Lox keywords translated to German
- **German Error Messages**: Runtime and parsing errors displayed in German
- **UTF-8 Support**: Full support for German characters (ä, ö, ü, ß)
- **Interactive REPL**: Command-line interface with German prompts
- **File Execution**: Run German Lox scripts from files

## German Keywords (Deutsche Schlüsselwörter)

| English | German | Description |
|---------|--------|-------------|
| `and` | `und` | Logical AND |
| `class` | `klasse` | Class declaration |
| `else` | `sonst` | Else clause |
| `false` | `falsch` | Boolean false |
| `for` | `für` | For loop |
| `fun` | `funktion` | Function declaration |
| `if` | `wenn` | If statement |
| `nil` | `nichts` | Null value |
| `or` | `oder` | Logical OR |
| `print` | `drucke` | Print statement |
| `return` | `zurückgeben` | Return statement |
| `super` | `super` | Superclass reference |
| `this` | `dies` | Instance reference |
| `true` | `wahr` | Boolean true |
| `var` | `var` | Variable declaration |
| `while` | `während` | While loop |

## Installation & Usage (Installation & Verwendung)

### Prerequisites
- Java 8 or higher
- Any Java IDE (Eclipse, IntelliJ, etc.) or command line

### Compilation
```bash
javac LOX/*.java
```

### Running the Interpreter

#### Interactive Mode (REPL)
```bash
java LOX.Lox
```

#### File Mode
```bash
java LOX.Lox script.lox
```

## Example Code (Beispielcode)

### Basic Variables and Print
```lox
var name = "Welt";
drucke "Hallo, " + name + "!";
// Output: Hallo, Welt!
```

### Conditional Statements
```lox
var alter = 25;
wenn (alter >= 18) {
    drucke "Du bist volljährig!";
} sonst {
    drucke "Du bist minderjährig.";
}
```

### Functions
```lox
funktion grüße(name) {
    zurückgeben "Guten Tag, " + name + "!";
}

drucke grüße("Anna");
// Output: Guten Tag, Anna!
```

### Loops
```lox
var i = 1;
während (i <= 5) {
    drucke "Zähler: " + i;
    i = i + 1;
}
```

### Classes and Objects
```lox
klasse Person {
    init(name, alter) {
        dies.name = name;
        dies.alter = alter;
    }
    
    vorstellen() {
        drucke "Hallo, ich bin " + dies.name + " und " + dies.alter + " Jahre alt.";
    }
}

var person = Person("Max", 30);
person.vorstellen();
```

## Error Messages (Fehlermeldungen)

The interpreter provides helpful error messages in German:

### Runtime Errors
- `"Operanden müssen Zahlen sein."` - Operands must be numbers
- `"Kann nur Funktionen und Klassen aufrufen."` - Can only call functions and classes  
- `"Undefinierte Variable 'x'."` - Undefined variable 'x'

### Parse Errors
- `"Unerwartetes Zeichen."` - Unexpected character
- `"Unbeendete Zeichenkette."` - Unterminated string

## Built-in Functions (Eingebaute Funktionen)

- `uhr()` - Returns current time in seconds (equivalent to English `clock()`)

## Project Structure (Projektstruktur)

```
LOX/
├── Lox.java           # Main interpreter class
├── Scanner.java       # Lexical analyzer with German keywords
├── Parser.java        # Parser with German error messages
├── Interpreter.java   # Interpreter with German runtime messages
├── Token.java         # Token representation
├── TokenType.java     # Token type definitions
├── Stmt.java          # Statement AST classes
├── Expr.java          # Expression AST classes
├── Environment.java   # Variable environment
├── RuntimeError.java  # Runtime error handling
└── ...               # Additional support classes
```

## Eclipse Setup

1. Create new Java Project in Eclipse
2. Create package named `LOX`
3. Copy all Java files into the LOX package
4. Right-click `Lox.java` → Run As → Java Application
5. For file execution: Run → Run Configurations → Arguments → Add filename

## Contributing (Mitwirken)

Contributions are welcome! Areas for improvement:
- Additional German language constructs
- Better error message localization
- German documentation
- Test cases with German examples

## Acknowledgments (Danksagungen)

- **Bob Nystrom** for the original Lox language and "Crafting Interpreters" book
- Licensed under Creative Commons BY-NC-SA 4.0
- German translation and localization by Aseel Taha

## License (Lizenz)

This project is based on material from "Crafting Interpreters" by Bob Nystrom, available at https://craftinginterpreters.com/, licensed under CC BY-NC-SA 4.0.

German localization modifications are also available under the same license.

---

**Viel Spaß beim Programmieren auf Deutsch!** (Have fun programming in German!)
