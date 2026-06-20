package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class PythonCommentNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final String text;

    public PythonCommentNode(String text, int lineNumber) {
        super("PythonCommentNode", lineNumber, 0);
        this.text = text;
    }

    public String getText() { return text; }

    @Override
    public String print(String indent) {
        return indent
                + "└── [PythonCommentNode]"
                + "  |  Text: " + text
                + "  |  Line: " + lineNumber + "\n";
    }
}

