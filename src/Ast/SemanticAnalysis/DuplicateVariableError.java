package Ast.SemanticAnalysis;

public class DuplicateVariableError extends SemanticError {
    public DuplicateVariableError(String varName, int line) {
        super("Duplicate variable declaration '" + varName + "'", line);
    }
}
