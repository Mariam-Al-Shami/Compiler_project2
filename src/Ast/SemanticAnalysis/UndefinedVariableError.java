package Ast.SemanticAnalysis;

public class UndefinedVariableError extends SemanticError {
    public UndefinedVariableError(String varName, int line) {
        super("Undefined variable '" + varName + "'", line);
    }
}
