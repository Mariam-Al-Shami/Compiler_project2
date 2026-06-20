package Ast.Jinja;

import Ast.ASTNode;
import Ast.BlockNode;
import Visitor.ASTVisitor;

public class JinjaForNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String loopVar;
    private final String iterable;
    private final BlockNode body;

    public JinjaForNode(String loopVar, String iterable, BlockNode body, int lineNumber) {
        super("JinjaForNode", lineNumber, 0);
        this.loopVar = loopVar;
        this.iterable = iterable;
        this.body = body;
    }

    public String getLoopVar() {
        return this.loopVar;
    }

    public String getIterable() {
        return this.iterable;
    }

    public BlockNode getBody() {
        return this.body;
    }

    public String toString() {
        return this.nodeName + "(for " + this.loopVar + " in " + this.iterable + ", line=" + this.lineNumber + ")";
    }

    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(this.nodeName).append(" [Line: ").append(this.lineNumber).append("]").append(" for ").append(this.loopVar).append(" in ").append(this.iterable).append("\n");
        sb.append(indent).append("  Body:\n");
        if (this.body != null) {
            sb.append(this.body.print(indent + "    "));
        }

        return sb.toString();
    }
}
