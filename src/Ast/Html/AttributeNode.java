package Ast.Html;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class AttributeNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String name;
    private final String value;

    public AttributeNode(String name, String value, int lineNumber) {
        super("AttributeNode", lineNumber, 0);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + (value != null ? "=\"" + value + "\"" : "");
    }

    @Override
    public String print(String indent) {
        return indent + "└── [Attribute] | " + name + "=\"" + value + "\" | Line: " + lineNumber + "\n";
    }
}