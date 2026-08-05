package Generator;

import Ast.*;
import Ast.Css.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Python.*;
import java.util.*;


public class AstJsonSerializer {

    public String toJson(ASTNode root) {
        StringBuilder sb = new StringBuilder();
        serializeNode(root, sb);
        return sb.toString();
    }

    public String toJson(Map<String, ASTNode> namedRoots) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, ASTNode> entry : namedRoots.entrySet()) {
            sb.append("  \"").append(escape(entry.getKey())).append("\": ");
            serializeNode(entry.getValue(), sb);
            i++;
            if (i < namedRoots.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private void serializeNode(ASTNode node, StringBuilder sb) {
        if (node == null) {
            sb.append("null");
            return;
        }

        String code = codeSnippet(node);
        List<ASTNode> children = childrenOf(node);

        sb.append("{ \"type\": \"").append(escape(node.getNodeName())).append("\"");
        sb.append(", \"line\": ").append(node.getLineNumber());
        if (!code.isEmpty()) {
            sb.append(", \"code\": \"").append(escape(code)).append("\"");
        }
        if (children != null && !children.isEmpty()) {
            sb.append(", \"children\": [");
            for (int i = 0; i < children.size(); i++) {
                serializeNode(children.get(i), sb);
                if (i < children.size() - 1) sb.append(", ");
            }
            sb.append("]");
        }
        sb.append(" }");
    }

    private String codeSnippet(ASTNode node) {
        if (node instanceof HtmlElementNode) return "<" + ((HtmlElementNode) node).getTagName() + ">";
        if (node instanceof HtmlSelfCloseNode) return "<" + ((HtmlSelfCloseNode) node).getTagName() + " />";
        if (node instanceof HtmlTextNode) return node.toString();
        if (node instanceof DoctypeNode) return "<!DOCTYPE " + ((DoctypeNode) node).getValue() + ">";
        if (node instanceof JinjaExprNode) return "{{ " + ((JinjaExprNode) node).getVariableName() + " }}";
        if (node instanceof JinjaForNode) {
            JinjaForNode f = (JinjaForNode) node;
            return "for " + f.getLoopVar() + " in " + f.getIterable();
        }
        if (node instanceof JinjaIfNode) return "if " + ((JinjaIfNode) node).getCondition();
        if (node instanceof PythonCommentNode) return ((PythonCommentNode) node).getText();
        if (node instanceof PythonImportNode) {
            PythonImportNode imp = (PythonImportNode) node;
            return imp.getKind() == PythonImportNode.ImportKind.IMPORT_MODULE
                    ? "import " + imp.getSource()
                    : "from " + imp.getSource() + " import " + imp.getNames();
        }
        if (node instanceof PythonAssignNode) return ((PythonAssignNode) node).getTarget() + " = ...";
        if (node instanceof PythonDecoratorNode) {
            PythonDecoratorNode d = (PythonDecoratorNode) node;
            return "@" + d.getPath() + "(" + d.getArguments() + ")";
        }
        if (node instanceof PythonFunctionNode) {
            PythonFunctionNode f = (PythonFunctionNode) node;
            return "def " + f.getName() + "(" + f.getParams() + ")";
        }
        if (node instanceof PythonReturnNode) return "return " + ((PythonReturnNode) node).getRawText();
        if (node instanceof PythonCallNode) {
            PythonCallNode c = (PythonCallNode) node;
            return c.getCallee() + "(" + c.getArgs() + ")";
        }
        if (node instanceof PythonExprNode) {
            PythonExprNode e = (PythonExprNode) node;
            return e.getRawText() != null ? e.getRawText() : "";
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private List<ASTNode> childrenOf(ASTNode node) {
        if (node instanceof ProgramNode) return ((ProgramNode) node).getStatements();
        if (node instanceof BlockNode) return ((BlockNode) node).getChildren();
        if (node instanceof HtmlElementNode) {
            HtmlElementNode h = (HtmlElementNode) node;
            List<ASTNode> ch = new ArrayList<>(h.getAttributes());
            if (h.getContent() != null) ch.addAll(h.getContent().getChildren());
            return ch;
        }
        if (node instanceof HtmlContentNode) return ((HtmlContentNode) node).getChildren();
        if (node instanceof HtmlBlockNode) {
            HtmlBlockNode b = (HtmlBlockNode) node;
            List<ASTNode> ch = new ArrayList<>();
            if (b.getOpening() != null) ch.add(b.getOpening());
            if (b.getContent() != null) ch.add(b.getContent());
            if (b.getClosing() != null) ch.add(b.getClosing());
            return ch;
        }
        if (node instanceof CssBlockNode) return ((CssBlockNode) node).getChildren();
        if (node instanceof JinjaForNode) {
            JinjaForNode f = (JinjaForNode) node;
            return f.getBody() != null ? f.getBody().getChildren() : null;
        }
        if (node instanceof JinjaIfNode) {
            JinjaIfNode jf = (JinjaIfNode) node;
            List<ASTNode> ch = new ArrayList<>();
            if (jf.getThenBlock() != null) ch.addAll(jf.getThenBlock().getChildren());
            if (jf.getElseBlock() != null) ch.addAll(jf.getElseBlock().getChildren());
            return ch;
        }
        if (node instanceof PythonFunctionNode) {
            PythonFunctionNode f = (PythonFunctionNode) node;
            return f.getBody() != null ? f.getBody().getChildren() : null;
        }
        if (node instanceof PythonIfNode) {
            PythonIfNode i = (PythonIfNode) node;
            List<ASTNode> ch = new ArrayList<>();
            if (i.getCondition() != null) ch.add(i.getCondition());
            if (i.getThenBlock() != null) ch.add(i.getThenBlock());
            if (i.getElseBlock() != null) ch.add(i.getElseBlock());
            return ch;
        }
        if (node instanceof PythonForNode) {
            PythonForNode f = (PythonForNode) node;
            return f.getBody() != null ? f.getBody().getChildren() : null;
        }
        if (node instanceof PythonAssignNode) {
            ASTNode v = ((PythonAssignNode) node).getValue();
            return v != null ? List.of(v) : null;
        }
        if (node instanceof PythonReturnNode) {
            ASTNode v = ((PythonReturnNode) node).getValue();
            return v != null ? List.of(v) : null;
        }
        if (node instanceof PythonExprNode) {
            PythonExprNode e = (PythonExprNode) node;
            if (e.getKind() == PythonExprNode.ExprKind.BINARY) {
                List<ASTNode> ch = new ArrayList<>();
                if (e.getLeft() != null) ch.add(e.getLeft());
                if (e.getRight() != null) ch.add(e.getRight());
                return ch;
            }
        }
        return null;
    }

    private String escape(Object o) {
        if (o == null) return "";
        String s = o.toString();
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
