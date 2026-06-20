package Ast.Python;

import Ast.ASTNode;
import Ast.BlockNode;
import Visitor.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

public class PythonFunctionNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private final String       name;
    private final List<String> params;
    private final BlockNode    body;

    public PythonFunctionNode(String name, List<String> params,
                              BlockNode body, int lineNumber) {
        super("PythonFunctionNode", lineNumber, 0);
        this.name   = name;
        this.params = (params != null) ? params : new ArrayList<>();
        this.body   = body;
    }

    public String       getName()   { return name; }
    public List<String> getParams() { return params; }
    public BlockNode    getBody()   { return body; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonFunctionNode]")
                .append("  |  Name: ").append(name)
                .append("  |  Params: ").append(params)
                .append("  |  Line: ").append(lineNumber).append("\n");
        if (body != null) {
            sb.append(body.print(indent + "    "));
        }
        return sb.toString();
    }
}
