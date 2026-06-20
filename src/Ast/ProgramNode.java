package Ast;

import java.util.ArrayList;
import java.util.List;
import Ast.Css.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Python.*;
import Visitor.ASTVisitor;

public class ProgramNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    private List<ASTNode> statements ;

    public ProgramNode(int lineNumber) {
        super("Program", lineNumber, 0);
        this.statements = new ArrayList<>();
    }

    public void addStatement(ASTNode stmt) {
        statements.add(stmt);
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    public String  print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Node: ").append(nodeName)
                .append(" [Line: ").append(lineNumber).append("]\n");
        for (ASTNode stmt : statements) {
            sb.append(stmt.print(indent + "  "));
        }
        return sb.toString();
    }
    public void printDetailedTree() {
        printDetailedTree(this, "", true);
    }

    private void printDetailedTree(ASTNode node, String prefix, boolean isLast) {
        if (node == null) return;
        String codeSnippet = "";
        if (node instanceof HtmlElementNode) {
            HtmlElementNode h = (HtmlElementNode)node;
            codeSnippet = "<" + h.getTagName() + ">";
        } else if (node instanceof HtmlSelfCloseNode) {
            codeSnippet = "<" + ((HtmlSelfCloseNode)node).getTagName() + " />";
        } else if (node instanceof HtmlTextNode) {
            codeSnippet = "\"" + ((HtmlTextNode)node).toString() + "\"";
        } else if (node instanceof DoctypeNode) {
            codeSnippet = "<!DOCTYPE " + ((DoctypeNode)node).getValue() + ">";
        }
        //css
        else if (node instanceof SelectorNode) {
            SelectorNode s = (SelectorNode)node;
            codeSnippet = s.getName();
            if (s.getPseudoClass() != null) {
                codeSnippet = codeSnippet + ":" + s.getPseudoClass();
            }
        } else if (node instanceof ClassSelectorNode) {
            ClassSelectorNode c = (ClassSelectorNode)node;
            String cls = c.getClassName();
            codeSnippet = cls;
            if (c.getPseudoSelector() != null) {
                String pseudo = c.getPseudoSelector().getName();
                if (pseudo.equals("hover")) {
                    codeSnippet = cls + ":" + pseudo;
                } else {
                    codeSnippet = cls + " " + pseudo;
                }
            }
        } else if (node instanceof DeclarationNode) {
            DeclarationNode d = (DeclarationNode)node;
            codeSnippet = d.getProperty() + ": ";
            if (d.getValues() != null) {
                for(int i = 0; i < d.getValues().size(); ++i) {
                    codeSnippet = codeSnippet + ((ValueNode)d.getValues().get(i)).getValue();
                    if (i < d.getValues().size() - 1) {
                        codeSnippet = codeSnippet + " ";
                    }
                }
            }

            codeSnippet = codeSnippet + ";";
        }else if (node instanceof JinjaExprNode) {
            codeSnippet = "{{ " + ((JinjaExprNode)node).getVariableName() + " }}";
        } else if (node instanceof JinjaForNode) {
            JinjaForNode f = (JinjaForNode)node;
            String var10000 = f.getLoopVar();
            codeSnippet = "for " + var10000 + " in " + f.getIterable();
        } else if (node instanceof JinjaIfNode) {
            codeSnippet = "if " + ((JinjaIfNode)node).getCondition();
        }
        // Python
        else if (node instanceof PythonCommentNode)
            codeSnippet = ((PythonCommentNode) node).getText();
        else if (node instanceof PythonImportNode) {
            PythonImportNode imp = (PythonImportNode) node;
            codeSnippet = imp.getKind() == PythonImportNode.ImportKind.IMPORT_MODULE
                    ? "import " + imp.getSource()
                    : "from "   + imp.getSource() + " import " + imp.getNames();
        }
        else if (node instanceof PythonAssignNode) {
            PythonAssignNode a = (PythonAssignNode) node;
            codeSnippet = a.getTarget() + " = ...";
        }
        else if (node instanceof PythonDecoratorNode) {
            PythonDecoratorNode d = (PythonDecoratorNode) node;
            codeSnippet = "@" + d.getPath() + "(" + d.getArguments() + ")";
        }
        else if (node instanceof PythonFunctionNode)
            codeSnippet = "def " + ((PythonFunctionNode) node).getName()
                    + "(" + ((PythonFunctionNode) node).getParams() + ")";
        else if (node instanceof PythonIfNode)
            codeSnippet = "if ...";
        else if (node instanceof PythonReturnNode)
            codeSnippet = "return " + ((PythonReturnNode) node).getRawText();
        else if (node instanceof PythonCallNode)
            codeSnippet = ((PythonCallNode) node).getCallee()
                    + "(" + ((PythonCallNode) node).getArgs() + ")";
        else if (node instanceof PythonExprNode) {
            PythonExprNode e = (PythonExprNode) node;
            codeSnippet = (e.getKind() == PythonExprNode.ExprKind.BINARY)
                    ? "(" + (e.getLeft()  != null ? e.getLeft().getNodeName()  : "?")
                    + " " + e.getOperator()
                    + " " + (e.getRight() != null ? e.getRight().getNodeName() : "?") + ")"
                    : (e.getRawText() != null ? e.getRawText() : "");
        }
        String connector = isLast ? "└── " : "├── ";
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(connector).append("[").append(node.getNodeName()).append("]");
        if (!codeSnippet.isEmpty()) sb.append("  |  Code: ").append(codeSnippet);
        sb.append("  |  Line: ").append(node.getLineNumber());
        System.out.println(sb.toString());

        List<ASTNode> children = null;

        if (node instanceof HtmlElementNode) {
            if (((HtmlElementNode)node).getContent() != null) {
                children = ((HtmlElementNode)node).getContent().getChildren();
            }
        } else if (node instanceof ProgramNode) {
            children = ((ProgramNode)node).getStatements();
        } else if (node instanceof BlockNode) {
            children = ((BlockNode)node).getChildren();
        } else if (node instanceof CssBlockNode) {
            children = ((CssBlockNode)node).getChildren();
        } else if (node instanceof SelectorRuleNode) {
            SelectorRuleNode ruleNode = (SelectorRuleNode)node;
            children = new ArrayList();
            if (ruleNode.getSelectors() != null) {
                ((List)children).add(ruleNode.getSelectors());
            }

            if (ruleNode.getDeclarations() != null) {
                ((List)children).addAll(ruleNode.getDeclarations());
            }
        } else if (node instanceof SelectorListNode) {
            children = new ArrayList(((SelectorListNode)node).getSelectors());
        } else if (node instanceof JinjaForNode) {
            JinjaForNode f = (JinjaForNode)node;
            if (f.getBody() != null) {
                children = f.getBody().getChildren();
            }
        } else if (node instanceof JinjaIfNode) {
            JinjaIfNode jinjaIf = (JinjaIfNode)node;
            children = new ArrayList();
            if (jinjaIf.getThenBlock() != null) {
                ((List)children).addAll(jinjaIf.getThenBlock().getChildren());
            }

            if (jinjaIf.getElseBlock() != null) {
                ((List)children).addAll(jinjaIf.getElseBlock().getChildren());
            }
        }

            // Python
        else if (node instanceof PythonFunctionNode) {
            PythonFunctionNode fn = (PythonFunctionNode) node;
            if (fn.getBody() != null) children = fn.getBody().getChildren();
        }
        else if (node instanceof PythonIfNode) {
            PythonIfNode ifNode = (PythonIfNode) node;
            children = new ArrayList<>();
            if (ifNode.getCondition() != null) children.add(ifNode.getCondition());
            if (ifNode.getThenBlock() != null) children.add(ifNode.getThenBlock());
            if (ifNode.getElseBlock() != null) children.add(ifNode.getElseBlock());
        }
        else if (node instanceof PythonAssignNode) {
            PythonAssignNode a = (PythonAssignNode) node;
            if (a.getValue() != null) {
                children = new ArrayList<>();
                children.add(a.getValue());
            }
        }
        else if (node instanceof PythonReturnNode) {
            PythonReturnNode r = (PythonReturnNode) node;
            if (r.getValue() != null) {
                children = new ArrayList<>();
                children.add(r.getValue());
            }
        }
        else if (node instanceof PythonExprNode) {
            PythonExprNode e = (PythonExprNode) node;
            if (e.getKind() == PythonExprNode.ExprKind.BINARY) {
                children = new ArrayList<>();
                if (e.getLeft()  != null) children.add(e.getLeft());
                if (e.getRight() != null) children.add(e.getRight());
            }
        }

        String childPrefix = prefix + (isLast ? "    " : "│   ");
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                boolean childIsLast = (i == children.size() - 1);
                printDetailedTree(children.get(i), childPrefix, childIsLast);
            }
        }
    }
}