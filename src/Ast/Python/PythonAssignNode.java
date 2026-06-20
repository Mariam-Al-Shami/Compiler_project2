package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;

public class PythonAssignNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public enum AssignKind { VAR, INDEXED, CALL }

    private final AssignKind kind;
    private final String     target;
    private final ASTNode    value;

    public PythonAssignNode(AssignKind kind, String target,
                            ASTNode value, int lineNumber) {
        super("PythonAssignNode", lineNumber, 0);
        this.kind   = kind;
        this.target = target;
        this.value  = value;
    }

    public AssignKind getKind()   { return kind; }
    public String     getTarget() { return target; }
    public ASTNode    getValue()  { return value; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonAssignNode]")
                .append("  |  Kind: ").append(kind)
                .append("  |  Target: ").append(target)
                .append("  |  Line: ").append(lineNumber).append("\n");
        if (value != null) {
            sb.append(value.print(indent + "    "));
        }
        return sb.toString();
    }
}
