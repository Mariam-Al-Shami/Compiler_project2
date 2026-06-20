package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

public class PythonDecoratorNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final String       path;
    private final List<String> arguments;

    public PythonDecoratorNode(String path, List<String> arguments, int lineNumber) {
        super("PythonDecoratorNode", lineNumber, 0);
        this.path      = path;
        this.arguments = (arguments != null) ? arguments : new ArrayList<>();
    }

    public String       getPath()      { return path; }
    public List<String> getArguments() { return arguments; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonDecoratorNode]")
                .append("  |  Path: @").append(path)
                .append("  |  Args: ").append(arguments)
                .append("  |  Line: ").append(lineNumber).append("\n");
        return sb.toString();
    }
}
