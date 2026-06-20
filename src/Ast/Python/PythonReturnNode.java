package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class PythonReturnNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final ASTNode value;
    private final String  rawText;

    public PythonReturnNode(ASTNode value, String rawText, int lineNumber) {
        super("PythonReturnNode", lineNumber, 0);
        this.value   = value;
        this.rawText = rawText;
    }

    public ASTNode getValue()   { return value; }
    public String  getRawText() { return rawText; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonReturnNode]")
                .append("  |  Line: ").append(lineNumber).append("\n");
        if (value != null) {
            sb.append(value.print(indent + "    "));
        } else if (rawText != null) {
            sb.append(indent).append("    Value: ").append(rawText).append("\n");
        }
        return sb.toString();
    }
}
