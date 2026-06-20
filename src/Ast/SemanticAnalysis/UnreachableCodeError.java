package Ast.SemanticAnalysis;

public class UnreachableCodeError extends SemanticError {
    public UnreachableCodeError(int line) {
        super("Unreachable code detected", line);
    }
}