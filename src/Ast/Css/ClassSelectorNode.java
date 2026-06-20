package Ast.Css;

import Ast.ASTNode;
import Visitor.ASTVisitor;

import java.util.List;

public class ClassSelectorNode extends SelectorRuleNode {

    private final String className;
    private final SelectorNode pseudoSelector;
    public ClassSelectorNode(String className,
                             SelectorNode pseudoSelector,
                             List<DeclarationNode> declarations,
                             int lineNumber) {
        super(null, declarations, lineNumber);

        this.className = className;
        this.pseudoSelector = pseudoSelector;
    }

    public String getClassName() {
        return className;
    }

    public SelectorNode getPseudoSelector() {
        return pseudoSelector;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Class Selector: ").append(className);
        if (pseudoSelector != null) {
            sb.append(":").append(pseudoSelector.getName());
        }
        sb.append(" [Line: ").append(lineNumber).append("]\n");
        if (getDeclarations() != null) {
            for (DeclarationNode decl : getDeclarations()) {
                sb.append(decl.print(indent + "    "));
            }
        }

        return sb.toString();
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}