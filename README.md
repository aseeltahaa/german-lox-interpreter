# German Lox Interpreter

A German localized version of the Lox programming language interpreter, based on Bob Nystrom's *Crafting Interpreters*. This dynamically-typed, object-oriented language has all keywords, error messages, and user-facing content translated into German while maintaining full compatibility with original Lox features.

---

## Features

* **German Keywords**: All Lox keywords translated to German
* **German Error Messages**: Runtime and parsing errors displayed in German
* **UTF-8 Support**: Full support for German characters (ä, ö, ü, ß)
* **Interactive REPL**: Command-line interface with German prompts
* **File Execution**: Run German Lox scripts from files
* **Dynamically Typed**: Variables do not require explicit types; types are determined at runtime
* **Object-Oriented**: Supports classes, objects, inheritance, and methods

---

## Syntax

| English  | German        | Description          |
| -------- | ------------- | -------------------- |
| `and`    | `und`         | Logical AND          |
| `class`  | `klasse`      | Class declaration    |
| `else`   | `sonst`       | Else clause          |
| `false`  | `falsch`      | Boolean false        |
| `for`    | `für`         | For loop             |
| `fun`    | `funktion`    | Function declaration |
| `if`     | `wenn`        | If statement         |
| `nil`    | `nichts`      | Null value           |
| `or`     | `oder`        | Logical OR           |
| `print`  | `drucke`      | Print statement      |
| `return` | `zurückgeben` | Return statement     |
| `super`  | `super`       | Superclass reference |
| `this`   | `dies`        | Instance reference   |
| `true`   | `wahr`        | Boolean true         |
| `var`    | `var`         | Variable declaration |
| `while`  | `während`     | While loop           |

---

## How It Works

The interpreter processes German Lox code in several stages:

```
Source Code
   ↓
Scanner (Lexer)
   ↓
Parser
   ↓
AST (Abstract Syntax Tree)
   ↓
Interpreter
   ↓
Program Output
```

**Step Description:**

1. **Scanner (Lexer):** Converts source code into a list of tokens (keywords, identifiers, symbols, numbers, strings).
2. **Parser:** Builds an **Abstract Syntax Tree (AST)** from the tokens according to German Lox grammar.
3. **AST:** Represents the hierarchical structure of the program.
4. **Interpreter:** Walks the AST, evaluating expressions and executing statements.
5. **Program Output:** Prints results or errors in German to the console.

---

## Context Free Grammer (CFG)

```
program     → declaration* EOF ;

declaration → varDecl
            | funDecl
            | classDecl
            | statement ;

varDecl     → "var" IDENTIFIER ( "=" expression )? ";" ;
funDecl     → "fun" function ;
classDecl   → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;

function    → IDENTIFIER "(" parameters? ")" block ;
parameters  → IDENTIFIER ( "," IDENTIFIER )* ;

statement   → exprStmt
            | ifStmt
            | printStmt
            | whileStmt
            | forStmt
            | returnStmt
            | block ;

exprStmt    → expression ";" ;
printStmt   → "print" expression ";" ;
returnStmt  → "return" expression? ";" ;
ifStmt      → "if" "(" expression ")" statement
              ( "else" statement )? ;
whileStmt   → "while" "(" expression ")" statement ;
forStmt     → "for" "(" ( varDecl | exprStmt | ";" )
                      expression? ";"
                      expression? ")" statement ;

block       → "{" declaration* "}" ;

expression  → assignment ;
assignment  → ( call "." )? IDENTIFIER "=" assignment 
            | logic_or ;

logic_or    → logic_and ( "or" logic_and )* ;
logic_and   → equality ( "and" equality )* ;
equality    → comparison ( ( "!=" | "==" ) comparison )* ;
comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term        → factor ( ( "-" | "+" ) factor )* ;
factor      → unary ( ( "/" | "*" ) unary )* ;
unary       → ( "!" | "-" ) unary | call ;
call        → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;

arguments   → expression ( "," expression )* ;

primary     → "true" | "false" | "nil"
            | NUMBER | STRING | IDENTIFIER
            | "(" expression ")" | "super" "." IDENTIFIER
            | "this" ;
```

---

## Installation & Usage

### Prerequisites

* Java 8 or higher
* Any Java IDE (Eclipse, IntelliJ, etc.) or command line

### Download

```bash
git clone https://github.com/yourusername/GermanLox.git
cd GermanLox
```

### Compilation

```bash
javac LOX/*.java
```

### Running the Interpreter

**Interactive Mode (REPL):**

```bash
java LOX.Lox
```

**File Mode:**

```bash
java LOX.Lox script.lox
```

**Example Code:**

```lox
var name = "Welt";
drucke "Hallo, " + name + "!";
```

Output:

```
Hallo, Welt!
```

---

## Error Messages

### Runtime Errors

* `"Operanden müssen Zahlen sein."` - Operands must be numbers
* `"Kann nur Funktionen und Klassen aufrufen."` - Can only call functions and classes
* `"Undefinierte Variable 'x'."` - Undefined variable

### Parse Errors

* `"Unerwartetes Zeichen."` - Unexpected character
* `"Unbeendete Zeichenkette."` - Unterminated string

---

## Built-in Functions

* `uhr()` – Returns current time in seconds (equivalent to English `clock()`)

---

## Acknowledgments 

* **Bob Nystrom** for the original Lox language and *Crafting Interpreters*
* Licensed under Creative Commons BY-NC-SA 4.0
* German localization by Aseel Taha

---

## License

This project is based on material from *Crafting Interpreters* by Bob Nystrom, licensed under CC BY-NC-SA 4.0. German localization modifications are also under the same license.
You can find the book at the following link: https://craftinginterpreters.com/
