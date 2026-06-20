package Ast.Css;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.List;

public class DeclarationNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String property;
    private final List<ValueNode> values;

    public DeclarationNode(String property, List<ValueNode> values, int lineNumber) {
        super("DeclarationNode", lineNumber, 0);
        this.property = property;
        this.values = values;
    }
    public String getProperty() { return property; }
    public List<ValueNode> getValues() { return values; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Property: ").append(property).append("\n");
        for (ValueNode val : values) {
            sb.append(val.print(indent + "    "));
        }
        return sb.toString();
    }
}