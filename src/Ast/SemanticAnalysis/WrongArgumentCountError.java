package Ast.SemanticAnalysis;

public class WrongArgumentCountError extends SemanticError {
    public WrongArgumentCountError(String funcName, int expected, int actual, int line) {
        super(
                "Function '" + funcName + "' expects " + expected +
                        " argument(s) but got " + actual,
                line
        );
    }
}