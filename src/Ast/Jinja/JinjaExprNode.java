package Ast.Jinja;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class JinjaExprNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String variableName;

    public JinjaExprNode(String variableName, int lineNumber) {
        super("JinjaExprNode", lineNumber, 0);
        this.variableName = variableName;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public String toString() {
        return this.nodeName + "(var=" + this.variableName + ", line=" + this.lineNumber + ")";
    }

    public String print(String indent) {
        return indent + this.nodeName + " [Line: " + this.lineNumber + "] {{ " + this.variableName + " }}\n";
    }
}
