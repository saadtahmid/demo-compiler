# AI Agent Instructions for Lox Compiler

This file provides context for AI coding assistants working in this repository.

## Project Context
- **Language/Framework**: Java
- **Domain**: A tree-walk interpreter for the "Lox" language (from *Crafting Interpreters*).
- **Core Components**: 
  - `Lox.java`: Main entry point (REPL and file runner).
  - `Token.java` & `TokenType.java`: Lexer/Scanner components.

## Guidelines
- **No Third-Party Dependencies**: This is a pure standard library Java project. Do not suggest adding Maven/Gradle dependencies unless explicitly requested.
- **Error Handling**: Use the existing `Lox.error()` and `Lox.report()` methods for syntax and runtime errors instead of throwing raw Java exceptions that crash the interpreter.

## Documentation Enforcement (README Updates)
Whenever a user asks you to implement a new feature (e.g., a new keyword, syntax, evaluation rule, or CLI flag), you **must**:
1. Check if the change introduces new user-facing functionality or Lox language features.
2. If so, proactively prompt the user or automatically update the [`README.md`](./README.md) to explain the new capability, its syntax, or any updated usage instructions (`java Lox [script]`).
3. Keep the `README.md` updated as the central "language specification" for this demo compiler.

## Post-Feature Workflow (Docs + Commit)
After implementing a feature or notable change, run this cycle automatically without waiting to be asked:
1. **Update docs** — sync `README.md` (and `CLAUDE.md` if architecture/commands changed). Regenerate `Expr.java`/`Stmt.java` via `tool/GenerateAst.java` if AST node types changed.
2. **Verify** — confirm the code still compiles (`javac *.java`).
3. **Commit** — stage the source + doc changes together and commit with a descriptive message; exclude `*.class` build artifacts. The doc update belongs in the same commit as the feature.

Only `git push` when the user explicitly requests it.
