package Ast.Python;

import Ast.ASTNode;
import Ast.BlockNode;
import Visitor.ASTVisitor;

public class PythonIfNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final ASTNode   condition;
    private final BlockNode thenBlock;
    private final BlockNode elseBlock;

    public PythonIfNode(ASTNode condition, BlockNode thenBlock,
                        BlockNode elseBlock, int lineNumber) {
        super("PythonIfNode", lineNumber, 0);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public ASTNode   getCondition() { return condition; }
    public BlockNode getThenBlock() { return thenBlock; }
    public BlockNode getElseBlock() { return elseBlock; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonIfNode]")
                .append("  |  Line: ").append(lineNumber).append("\n");

        if (condition != null) {
            sb.append(indent).append("    Condition:\n");
            sb.append(condition.print(indent + "        "));
        }
        if (thenBlock != null) {
            sb.append(indent).append("    Then:\n");
            sb.append(thenBlock.print(indent + "        "));
        }
        if (elseBlock != null) {
            sb.append(indent).append("    Else:\n");
            sb.append(elseBlock.print(indent + "        "));
        }
        return sb.toString();
    }
}
