package Ast.Jinja;

import Ast.ASTNode;
import Visitor.ASTVisitor;

import java.util.Iterator;
import java.util.List;

public class JinjaBlockNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final String name;
    private final List<ASTNode> statements;

    public JinjaBlockNode(String name, List<ASTNode> statements, int lineNumber) {
        super("JinjaBlockNode", lineNumber, 0);
        this.name = name;
        this.statements = statements;
    }

    public String getName() {
        return this.name;
    }

    public List<ASTNode> getStatements() {
        return this.statements;
    }

    public String toString() {
        String var10000 = this.nodeName;
        return var10000 + "(name=" + this.name + ", size=" + (this.statements != null ? this.statements.size() : 0) + ", line=" + this.lineNumber + ")";
    }

    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(this.nodeName).append(" [Line: ").append(this.lineNumber).append("] ").append("Block: ").append(this.name).append("\n");
        if (this.statements != null) {
            Iterator var3 = this.statements.iterator();

            while(var3.hasNext()) {
                ASTNode stmt = (ASTNode)var3.next();
                sb.append(stmt.print(indent + "  "));
            }
        }

        return sb.toString();
    }
}
