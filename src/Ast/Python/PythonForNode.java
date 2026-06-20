package Ast.Python;

import Ast.ASTNode;
import Ast.BlockNode;
import Visitor.ASTVisitor;

public class PythonForNode extends ASTNode {

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final String   loopVar;
    private final ASTNode  iterable;
    private final BlockNode body;

    public PythonForNode(String loopVar, ASTNode iterable,
                         BlockNode body, int lineNumber) {
        super("PythonForNode", lineNumber, 0);
        this.loopVar  = loopVar;
        this.iterable = iterable;
        this.body     = body;
    }

    public String    getLoopVar()  { return loopVar; }
    public ASTNode   getIterable() { return iterable; }
    public BlockNode getBody()     { return body; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonForNode]")
                .append("  |  for ").append(loopVar).append(" in ...")
                .append("  |  Line: ").append(lineNumber).append("\n");
        if (body != null) {
            sb.append(body.print(indent + "    "));
        }
        return sb.toString();
    }
}