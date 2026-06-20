package Ast.Css;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.List;

public class SelectorRuleNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private final ASTNode selectors;
    private final List<DeclarationNode> declarations;

    public SelectorRuleNode(ASTNode selectors,
                            List<DeclarationNode> declarations,
                            int lineNumber) {

        super("SelectorRuleNode", lineNumber, 0);

        this.selectors = selectors;
        this.declarations = declarations;
    }

    public ASTNode getSelectors() {
        return selectors; }
    public List<DeclarationNode> getDeclarations() { return declarations; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("CSS Rule [Line: ")
                .append(lineNumber)
                .append("]\n");

        if (selectors != null) {
            sb.append(selectors.print(indent + "  "));
        }

        sb.append(indent)
                .append("  Declarations:\n");

        for (DeclarationNode decl : declarations) {
            sb.append(decl.print(indent + "    "));
        }
        return sb.toString();
    }
}