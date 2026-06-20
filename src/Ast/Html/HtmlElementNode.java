package Ast.Html;
import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class HtmlElementNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String tagName;
    private final List<AttributeNode> attributes = new ArrayList<>();
    private HtmlContentNode content;

    public HtmlElementNode(String tagName, int lineNumber) {
        super("HtmlElementNode", lineNumber, 0);
        this.tagName = tagName;
        this.content = new HtmlContentNode(lineNumber);
    }

    public void addAttribute(AttributeNode attr) { attributes.add(attr); }
    public void addChild(ASTNode child) {
        this.content.addChild(child);
    }

    public String getTagName() { return tagName; }
    public List<AttributeNode> getAttributes() { return attributes; }
    public HtmlContentNode getContent() { return content; } // Getter للمحتوى

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<" + tagName);
        for (AttributeNode attr : attributes) {
            sb.append(" ").append(attr.toString());
        }
        sb.append(">");
        return sb.toString();
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("└── [HtmlElementNode] | Code: ").append(this.toString()).append(" | Line: ").append(lineNumber).append("\n");
        if (content != null) {
            sb.append(content.print(indent + "    "));
        }
        return sb.toString();
    }
}