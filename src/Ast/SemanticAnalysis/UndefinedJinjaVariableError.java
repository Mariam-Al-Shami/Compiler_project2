package Ast.SemanticAnalysis;

public class UndefinedJinjaVariableError extends SemanticError {
    public UndefinedJinjaVariableError(String varName, int line) {
        super("Undefined Jinja variable '" + varName + "'", line);
    }
}
