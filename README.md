# Java Lox Compiler

A tree-walk interpreter for the Lox programming language, written in pure Java. This project is based on the language specifications from *Crafting Interpreters*.

## Usage

You can run the interpreter in two modes:

### 1. Interactive REPL (Read-Eval-Print Loop)
Run the interpreter without any arguments to start an interactive prompt:
```bash
java Lox
```
This will open an interactive prompt (`>`) where you can enter Lox code line by line.

### 2. Script Runner
To execute a Lox script file, pass the file path as an argument:
```bash
java Lox [script]
```
Exit codes:
- `64`: Incorrect command-line usage.
- `65`: A syntax or data error occurred during execution.

## Generating the AST

The Abstract Syntax Tree node classes (`Expr.java` and `Stmt.java`) are **generated**, not hand-written. A small code-generation tool defines every node type from a compact description so the AST shape lives in one place and stays consistent.

Run it from the project root, passing the output directory:

```bash
javac tool/GenerateAst.java
java tool.GenerateAst .
```

Re-run this whenever AST node types are added or changed, then re-compile the interpreter. The generated classes use the **Visitor pattern**, so new passes (interpreter, resolver, pretty-printer) can be added without modifying the node classes.

## Language Specification & Features

The current implementation features the scanner (lexer) and the generated AST node definitions for expressions and statements. 

### Symbols & Operators
* **Single-character tokens**: `(`, `)`, `{`, `}`, `,`, `.`, `-`, `+`, `;`, `/`, `*`
* **One or two-character tokens**: `!`, `!=`, `=`, `==`, `>`, `>=`, `<`, `<=`

### Literals
* **Strings**: Surrounded by double quotes.
* **Numbers**: Floating-point numbers.
* **Identifiers**: User-defined variable, class, or function names.

### Keywords
The scanner recognizes the following reserved keywords:
* **Control Flow**: `if`, `else`, `for`, `while`
* **Logical & Boolean**: `and`, `or`, `true`, `false`, `nil`
* **Variables & Output**: `var`, `print`
* **Functions**: `fun`, `return`
* **Object-Oriented**: `class`, `this`, `super`

## Core Components
- **`Lox.java`**: Main entry point handling CLI logic, read-file operations, the interactive REPL, and top-level error reporting without crashing the runtime.
- **`Scanner.java`**: Responsible for the main lexical analysis (scanning). Reads the raw source string character by character, recognizes keywords, handles whitespace/comments (line comments `//`), and groups characters into a list of `Token` instances.
- **`Token.java`**: Represents a lexical token, storing its type, exact string (lexeme), literal runtime value, and source line number.
- **`TokenType.java`**: The enumeration characterizing all currently valid syntax tokens.
- **`tool/GenerateAst.java`**: Code-generation tool that produces the AST node classes from compact type descriptions (see [Generating the AST](#generating-the-ast)).
- **`Expr.java`** *(generated)*: Expression AST nodes — `Assign`, `Binary`, `Call`, `Get`, `Grouping`, `Literal`, `Logical`, `Set`, `Super`, `This`, `Unary`, `Variable`.
- **`Stmt.java`** *(generated)*: Statement AST nodes — `Block`, `Class`, `Expression`, `Function`, `If`, `Print`, `Return`, `Var`, `While`.
