package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Code-generation tool that writes Expr.java and Stmt.java into a given output directory.
 *
 * WHY THIS EXISTS:
 * The Lox AST has many node types (Binary, Unary, Call, etc.). Writing each as a
 * hand-coded class is tedious and error-prone. This tool generates all of them from a
 * compact description — one line per node type — so the shape of the AST is defined
 * in a single place and stays consistent.
 *
 * The generated files should be committed alongside the interpreter source.
 * Re-run this tool whenever AST node types are added or changed.
 *
 * COMPILE AND RUN (from the project root):
 *   javac tool/GenerateAst.java
 *   java tool.GenerateAst <output-directory>
 *
 * Example (generate into the project root):
 *   java tool.GenerateAst .
 */
public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        // Each string is "ClassName : Type field, Type field, ..."
        // defineAst() parses these descriptions and generates one nested class per entry.

        defineAst(outputDir, "Expr", Arrays.asList(
            // Assignment is an expression in Lox: `x = 5` produces a value.
            "Assign   : Token name, Expr value",

            // Binary covers arithmetic and comparison: +, -, *, /, ==, !=, <, >, <=, >=
            "Binary   : Expr left, Token operator, Expr right",

            // A function/method call: callee(arg1, arg2). `paren` holds the closing ')' for
            // error reporting line numbers.
            "Call     : Expr callee, Token paren, List<Expr> arguments",

            // Property access on an object: object.name
            "Get      : Expr object, Token name",

            // Parenthesised expression: (expr). Exists as its own node so the pretty-printer
            // and resolver can treat explicit grouping distinctly if needed.
            "Grouping : Expr expression",

            // A literal value baked in at parse time: number, string, true, false, nil.
            // `value` is already the Java runtime type (Double, String, Boolean, or null).
            "Literal  : Object value",

            // Short-circuit `and` / `or`. Kept separate from Binary because the right
            // operand must not be evaluated until the left operand is resolved.
            "Logical  : Expr left, Token operator, Expr right",

            // Property assignment on an object: object.name = value
            "Set      : Expr object, Token name, Expr value",

            // `super.method` — keyword carries the line number; method is the accessed name.
            "Super    : Token keyword, Token method",

            // `this` reference inside a method body.
            "This     : Token keyword",

            // Unary prefix operators: `-` (negate) and `!` (logical not).
            "Unary    : Token operator, Expr right",

            // A reference to a variable by name; resolved to its value at runtime.
            "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            // A braced block { ... } creates a new lexical scope.
            "Block      : List<Stmt> statements",

            // Class declaration. `superclass` is null when there is no `< Parent`.
            "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",

            // An expression used as a statement (e.g. a function call on its own line).
            "Expression : Expr expression",

            // Function declaration: `fun name(params) { body }`.
            "Function   : Token name, List<Token> params, List<Stmt> body",

            // if/else. `elseBranch` is null when there is no else clause.
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",

            // `print` statement — built-in output in Lox.
            "Print      : Expr expression",

            // `return` statement. `keyword` carries the line number for error reporting.
            // `value` is null for a bare `return;`.
            "Return     : Token keyword, Expr value",

            // Variable declaration: `var name = initializer;`. `initializer` is null when
            // the variable is declared without a value (implicitly nil).
            "Var        : Token name, Expr initializer",

            // while loop. `for` loops are desugared into While nodes by the parser.
            "While      : Expr condition, Stmt body"
        ));
    }

    /**
     * Generates a single Java source file containing:
     *   - An abstract base class named `baseName`
     *   - A Visitor<R> interface (one visit method per concrete subtype)
     *   - One static nested class per entry in `types`
     *
     * The Visitor pattern lets new operations (interpreting, pretty-printing,
     * static analysis) be added without modifying any AST node class. The
     * compiler enforces completeness: if a new node type is added, every
     * Visitor implementation fails to compile until it handles the new case.
     */
    private static void defineAst(String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        writer.println();

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields    = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The abstract accept() on the base class forces every subclass to override it,
        // which in turn forces the corresponding visitor method to exist — compile-time
        // proof that no node type is silently ignored by any pass.
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");

        writer.close();
        System.out.println("Generated: " + path);
    }

    /**
     * Writes the Visitor<R> interface inside the base class.
     *
     * Naming convention: visit + SubclassName + BaseName
     *   e.g. visitBinaryExpr, visitPrintStmt
     *
     * The base-class name suffix lets a single object implement both Expr.Visitor
     * and Stmt.Visitor without method-name collisions.
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "("
                    + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
        writer.println();
    }

    /**
     * Writes a single concrete subclass with:
     *   - final fields for every entry in fieldList
     *   - a constructor that assigns all fields
     *   - an accept() override that dispatches to the correct visitor method
     *
     * Each subclass is static so it can be instantiated without a reference to
     * the outer Expr/Stmt class.
     */
    private static void defineType(PrintWriter writer, String baseName,
                                   String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");
        writer.println();

        // Constructor.
        writer.println("        " + className + "(" + fieldList + ") {");
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            // field format is "Type name" — grab the name after the last space so generic
            // types like List<Expr> are handled correctly.
            String name = field.substring(field.lastIndexOf(' ') + 1);
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");
        writer.println();

        // accept() routes this node to exactly one visitor method. The subclass knows
        // its own type, so the visitor never needs instanceof checks.
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");
        writer.println();

        // Declare each field as final — AST nodes are immutable after construction.
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }
        writer.println("    }");
        writer.println();
    }
}
