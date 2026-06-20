package Ast.SemanticAnalysis;

public class InvalidTypeError extends SemanticError {
    public InvalidTypeError(String message, int line) {
        super("Invalid type: " + message, line);
    }
}
