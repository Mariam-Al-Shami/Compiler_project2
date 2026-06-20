package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class PythonExprNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public enum ExprKind {
        STRING, BOOL, NONE, IDENTIFIER,
        BINARY, CALL, LIST, BUILTIN
    }

    private final ExprKind kind;
    private final String   rawText;
    private final ASTNode  left;
    private final String   operator;
    private final ASTNode  right;

    public PythonExprNode(ExprKind kind, String rawText, int lineNumber) {
        super("PythonExprNode", lineNumber ,0);
        this.kind     = kind;
        this.rawText  = rawText;
        this.left     = null;
        this.operator = null;
        this.right    = null;
    }

    public PythonExprNode(ASTNode left, String operator,
                          ASTNode right, int lineNumber) {
        super("PythonExprNode", lineNumber, 0);
        this.kind     = ExprKind.BINARY;
        this.rawText  = null;
        this.left     = left;
        this.operator = operator;
        this.right    = right;
    }

    public ExprKind getKind()     { return kind; }
    public String   getRawText()  { return rawText; }
    public ASTNode  getLeft()     { return left; }
    public String   getOperator() { return operator; }
    public ASTNode  getRight()    { return right; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonExprNode]")
                .append("  |  Kind: ").append(kind);

        if (kind == ExprKind.BINARY) {
            sb.append("  |  Op: ").append(operator)
                    .append("  |  Line: ").append(lineNumber).append("\n");
            if (left  != null) sb.append(left.print(indent  + "    "));
            if (right != null) sb.append(right.print(indent + "    "));
        } else {
            sb.append("  |  Value: ").append(rawText)
                    .append("  |  Line: ").append(lineNumber).append("\n");
        }
        return sb.toString();
    }
}

