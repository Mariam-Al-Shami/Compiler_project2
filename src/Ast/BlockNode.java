package Ast;

import Visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class BlockNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final List<ASTNode> children;
    public BlockNode(int line) {
        this(new ArrayList<>(), line);
    }
    public BlockNode(List<ASTNode> children, int line) {
        super("Block", line, 0);
        this.children = (children != null) ? children : new ArrayList<>();
    }
    public List<ASTNode> getChildren() {
        return children;
    }
    public void addStatement(ASTNode stmt) {
        children.add(stmt);
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Block\n");
        for (ASTNode child : children) {
            if (child != null) {
                sb.append(child.print(indent + " "));
            }
        }
        return sb.toString();
    }
}
