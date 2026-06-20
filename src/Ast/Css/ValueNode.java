package Ast.Css;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class ValueNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String value;

    public ValueNode(String value, int lineNumber) {
        super("ValueNode", lineNumber, 0);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String print(String indent) {
        return indent + "Value: " + value + " [Line: " + lineNumber + "]\n";
    }
}