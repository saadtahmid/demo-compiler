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

This is a tree-walk interpreter for the Lox language, following *Crafting Interpreters*. The project currently has the **scanner** and the **generated AST node definitions** (`Expr`/`Stmt`); the parser is the next stage. The pipeline is:

```
source string → Scanner → List<Token> → (parser, not yet implemented) → Expr/Stmt AST
```

AST node classes are **generated** by `tool/GenerateAst.java`, not hand-edited. To change them, edit the type descriptions in that tool and regenerate:

```bash
javac tool/GenerateAst.java
java tool.GenerateAst .
```

- **`Lox.java`** — entry point; owns the `hadError` flag, the REPL loop, file reading, and the `error()`/`report()` methods. The `run()` method currently just scans and prints tokens.
- **`Scanner.java`** — consumes the raw source character by character using a `start`/`current`/`line` cursor. Produces a `List<Token>`. Handles string and number literals, identifiers, keywords (via a static `HashMap`), line comments (`//`), and whitespace.
- **`Token.java`** — value object: `TokenType type`, `String lexeme`, `Object literal`, `int line`.
- **`TokenType.java`** — enum of every valid token kind.

## Key Constraints

- **No third-party dependencies.** Pure Java standard library only. Do not introduce Maven, Gradle, or any external jars.
- **Error reporting**: always use `Lox.error(line, message)` (which calls `Lox.report()` and sets `hadError`). Do not throw raw Java exceptions for Lox-level errors.
- **README as language spec**: when adding new language features, keywords, syntax rules, or CLI flags, update `README.md` to reflect the change. The README is the canonical user-facing specification.

## Feature Workflow

After implementing any feature or notable change, complete the cycle without waiting to be asked:

1. **Update docs.** Reflect the change in `README.md` (the language spec) and, if architecture/components/commands changed, in this `CLAUDE.md`. If AST node types changed, regenerate `Expr.java`/`Stmt.java` via `tool/GenerateAst.java`.
2. **Verify it compiles.** Run `javac *.java` (and `javac tool/GenerateAst.java` if the tool changed).
3. **Commit.** Stage the relevant source and doc files together and commit with a descriptive message. Keep generated/build artifacts (`*.class`) out of the commit. Treat doc updates as part of the same feature commit, not an afterthought.

Do not push unless the user asks.
