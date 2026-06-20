package Ast.Css;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class SelectorNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String name;
    private final String pseudoClass;

    public SelectorNode(String name, String pseudoClass, int lineNumber) {
        super("SelectorNode", lineNumber, 0);
        this.name = name;
        this.pseudoClass = pseudoClass;
    }

    public String getName() { return name; }
    public String getPseudoClass() { return pseudoClass; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Selector: ").append(name);
        if (pseudoClass != null) {
            sb.append(":").append(pseudoClass);
        }
        sb.append(" [Line: ").append(lineNumber).append("]\n");
        return sb.toString();
    }
}