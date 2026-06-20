package Ast.Html;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class DoctypeNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String value;

    public DoctypeNode(String value, int lineNumber) {
        super("DoctypeNode", lineNumber, 0);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent);
        sb.append("DOCTYPE: ");
        sb.append(value);
        sb.append(" [Line: ");
        sb.append(lineNumber);
        sb.append("]\n");

        return sb.toString();
    }
    @Override
    public String toString() {
        return "<!DOCTYPE " + value + ">";
    }
}