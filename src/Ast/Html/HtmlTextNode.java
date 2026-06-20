package Ast.Html;
import Ast.ASTNode;
import Visitor.ASTVisitor;

public class HtmlTextNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String text;
    public HtmlTextNode(String text, int lineNumber) {
        super("TextNode", lineNumber, 0);
        this.text = text;
    }
    @Override
    public String toString() {
        return text.trim();
    }

    @Override
    public String print(String indent) {
        if (text.trim().isEmpty()) return "";
        return indent + "└── [TextNode] | Code: " + text.trim() + " | Line: " + lineNumber + "\n";
    }
    public String getText() {
        return text;
    }
}