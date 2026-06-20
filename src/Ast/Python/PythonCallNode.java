package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

public class PythonCallNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final String       callee;
    private final List<String> args;

    public PythonCallNode(String callee, List<String> args, int lineNumber) {
        super("PythonCallNode", lineNumber, 0);
        this.callee = callee;
        this.args   = (args != null) ? args : new ArrayList<>();
    }

    public String       getCallee() { return callee; }
    public List<String> getArgs()   { return args; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonCallNode]")
                .append("  |  Callee: ").append(callee)
                .append("  |  Args: ").append(args)
                .append("  |  Line: ").append(lineNumber).append("\n");
        return sb.toString();
    }
}
