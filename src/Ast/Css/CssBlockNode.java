package Ast.Css;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class CssBlockNode extends ASTNode {

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final List<ASTNode> children;

    public CssBlockNode(int lineNumber) {
        super("CssBlockNode", lineNumber, 0);
        this.children = new ArrayList<>();
    }

    public List<ASTNode> getChildren() {

        return children;
    }
    public void addRule(ASTNode rule) {  // ← ASTNode مش SelectorRuleNode
        this.children.add(rule);
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("CSS BLOCK [Line: ").append(lineNumber).append("]\n");
        for (ASTNode child : children) {
            sb.append(child.print(indent + "  "));
        }
        return sb.toString();
    }
}