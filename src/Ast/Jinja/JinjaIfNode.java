package Ast.Jinja;

import Ast.ASTNode;
import Ast.BlockNode;
import Visitor.ASTVisitor;

public class JinjaIfNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String condition;
    private final BlockNode thenBlock;
    private final BlockNode elseBlock;

    public JinjaIfNode(String condition, BlockNode thenBlock, BlockNode elseBlock, int lineNumber) {
        super("JinjaIfNode", lineNumber, 0);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public String getCondition() {
        return this.condition;
    }

    public BlockNode getThenBlock() {
        return this.thenBlock;
    }

    public BlockNode getElseBlock() {
        return this.elseBlock;
    }

    public String toString() {
        return this.nodeName + "(if " + this.condition + ", line=" + this.lineNumber + ")";
    }

    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(this.nodeName).append(" [Line: ").append(this.lineNumber).append("]").append(" if ").append(this.condition).append("\n");
        sb.append(indent).append("  Then:\n");
        if (this.thenBlock != null) {
            sb.append(this.thenBlock.print(indent + "    "));
        }

        if (this.elseBlock != null) {
            sb.append(indent).append("  Else:\n");
            sb.append(this.elseBlock.print(indent + "    "));
        }

        return sb.toString();
    }
}
