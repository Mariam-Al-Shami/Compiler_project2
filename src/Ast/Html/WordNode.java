package Ast.Html;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class WordNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String text;

    public WordNode(String text, int lineNumber) {
        super("WordNode", lineNumber, 0);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent);
        sb.append("Word: ");
        sb.append(text);
        sb.append(" [Line: ");
        sb.append(lineNumber);
        sb.append("]\n");

        return sb.toString();
    }
}