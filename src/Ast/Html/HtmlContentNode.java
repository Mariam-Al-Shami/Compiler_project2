package Ast.Html;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class HtmlContentNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final List<ASTNode> children;

    public HtmlContentNode(int lineNumber) {
        super("HtmlContentNode", lineNumber, 0);
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        this.children.add(child);
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent);
        sb.append("Content:\n");

        for (ASTNode child : children) {
            String childIndent = indent + "  ";
            sb.append(child.print(childIndent));
        }

        return sb.toString();
    }
}