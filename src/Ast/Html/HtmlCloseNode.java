package Ast.Html;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class HtmlCloseNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String tagName;

    public HtmlCloseNode(String tagName, int lineNumber) {
        super("HtmlCloseNode", lineNumber, 0);
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent);
        sb.append("Closing Tag: ");
        sb.append(tagName);
        sb.append(" [Line: ");
        sb.append(lineNumber);
        sb.append("]\n");

        return sb.toString();
    }
}