/**
 * Immutable record of a single lexical token.
 * `lexeme` is the exact source substring (e.g. "!=" or "hello").
 * `literal` holds the already-parsed runtime value: Double for NUMBER,
 * String for STRING (quotes stripped), null for every other token type.
 */
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
