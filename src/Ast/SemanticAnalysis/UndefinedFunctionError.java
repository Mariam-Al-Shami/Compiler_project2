package Ast.SemanticAnalysis;

public class UndefinedFunctionError extends SemanticError {
    public UndefinedFunctionError(String funcName, int line) {
        super("Undefined function '" + funcName + "'", line);
    }
}
