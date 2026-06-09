# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

No build tool — compile all Java files directly:

```bash
# Compile
javac *.java

# Run interactive REPL
java Lox

# Run a script file
java Lox path/to/script.lox
```

Exit codes: `64` = bad CLI usage, `65` = syntax/runtime error.

There are no automated tests. Manual testing is done via the REPL or script files.

## Architecture

This is a tree-walk interpreter for the Lox language, following *Crafting Interpreters*. The project is currently at the **scanner/lexer** stage. The pipeline is:

```
source string → Scanner → List<Token> → (parser, not yet implemented)
```

- **`Lox.java`** — entry point; owns the `hadError` flag, the REPL loop, file reading, and the `error()`/`report()` methods. The `run()` method currently just scans and prints tokens.
- **`Scanner.java`** — consumes the raw source character by character using a `start`/`current`/`line` cursor. Produces a `List<Token>`. Handles string and number literals, identifiers, keywords (via a static `HashMap`), line comments (`//`), and whitespace.
- **`Token.java`** — value object: `TokenType type`, `String lexeme`, `Object literal`, `int line`.
- **`TokenType.java`** — enum of every valid token kind.

## Key Constraints

- **No third-party dependencies.** Pure Java standard library only. Do not introduce Maven, Gradle, or any external jars.
- **Error reporting**: always use `Lox.error(line, message)` (which calls `Lox.report()` and sets `hadError`). Do not throw raw Java exceptions for Lox-level errors.
- **README as language spec**: when adding new language features, keywords, syntax rules, or CLI flags, update `README.md` to reflect the change. The README is the canonical user-facing specification.
