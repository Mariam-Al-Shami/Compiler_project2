package Generator;

import Ast.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Css.*;
import java.util.*;

public class JinjaGenerator {

    private final List<Map<String, String>> products;

    public JinjaGenerator(List<Map<String, String>> products) {
        this.products = products;
    }

    public String generate(ASTNode root) {
        StringBuilder sb = new StringBuilder();
        if (root instanceof ProgramNode) {
            for (ASTNode stmt : ((ProgramNode) root).getStatements()) {
                sb.append(generateNode(stmt));
            }
        }
        return sb.toString();
    }

    private String generateNode(ASTNode node) {
        if (node == null) return "";

        //HTML Nodes
        if (node instanceof DoctypeNode) {
            return "<!DOCTYPE html>\n";
        }

        if (node instanceof HtmlElementNode) {
            return generateHtmlElement((HtmlElementNode) node);
        }

        if (node instanceof HtmlSelfCloseNode) {
            return generateSelfClose((HtmlSelfCloseNode) node);
        }

        if (node instanceof HtmlTextNode) {
            return node.toString().trim().isEmpty() ? "" : node.toString().trim() + "\n";
        }

        if (node instanceof HtmlContentNode) {
            StringBuilder sb = new StringBuilder();
            for (ASTNode child : ((HtmlContentNode) node).getChildren()) {
                sb.append(generateNode(child));
            }
            return sb.toString();
        }

        if (node instanceof HtmlBlockNode) {
            HtmlBlockNode block = (HtmlBlockNode) node;
            StringBuilder sb = new StringBuilder();
            if (block.getOpening()  != null) sb.append(generateNode(block.getOpening()));
            if (block.getContent()  != null) sb.append(generateNode(block.getContent()));
            if (block.getClosing()  != null) sb.append(generateNode(block.getClosing()));
            return sb.toString();
        }

        //Jinja Nodes
        if (node instanceof JinjaForNode) {
            return generateJinjaFor((JinjaForNode) node);
        }

        if (node instanceof JinjaIfNode) {
            return generateJinjaIf((JinjaIfNode) node, null);
        }

        if (node instanceof JinjaExprNode) {
            return generateJinjaExpr((JinjaExprNode) node, null);
        }

        //CSS Block
        if (node instanceof CssBlockNode) {
            return generateCss((CssBlockNode) node);
        }

        //Block Node
        if (node instanceof BlockNode) {
            StringBuilder sb = new StringBuilder();
            for (ASTNode child : ((BlockNode) node).getChildren()) {
                sb.append(generateNode(child));
            }
            return sb.toString();
        }

        return "";
    }

    // HTML Element
    private String generateHtmlElement(HtmlElementNode node) {
        StringBuilder sb = new StringBuilder();
        String tag = node.getTagName();
        sb.append("<").append(tag);
        for (AttributeNode attr : node.getAttributes()) {
            sb.append(" ").append(attr.getName());
            if (attr.getValue() != null) {
                sb.append("=\"").append(attr.getValue()).append("\"");
            }
        }
        sb.append(">\n");

        if (node.getContent() != null) {
            for (ASTNode child : node.getContent().getChildren()) {
                sb.append(generateNode(child));
            }
        }

        sb.append("</").append(tag).append(">\n");
        return sb.toString();
    }

    // Self-Close Tag
    private String generateSelfClose(HtmlSelfCloseNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(node.getTagName());
        for (AttributeNode attr : node.getAttributes()) {
            sb.append(" ").append(attr.getName());
            if (attr.getValue() != null) {
                sb.append("=\"").append(attr.getValue()).append("\"");
            }
        }
        sb.append(" />\n");
        return sb.toString();
    }

    // Jinja For Loop
    private String generateJinjaFor(JinjaForNode node) {
        StringBuilder sb = new StringBuilder();
        String loopVar  = node.getLoopVar();
        String iterable = node.getIterable();

        sb.append("<!-- Jinja For: for ").append(loopVar)
                .append(" in ").append(iterable).append(" -->\n");

        if (products == null || products.isEmpty()) {
            sb.append("<!-- لا توجد بيانات لعرضها -->\n");
            return sb.toString();
        }

        for (Map<String, String> product : products) {
            if (node.getBody() != null) {
                for (ASTNode child : node.getBody().getChildren()) {
                    sb.append(generateNodeWithData(child, loopVar, product));
                }
            }
        }

        sb.append("<!-- endfor -->\n");
        return sb.toString();
    }

    // Jinja If

    private String generateJinjaIf(JinjaIfNode node,
                                   Map<String, String> currentProduct) {
        StringBuilder sb = new StringBuilder();
        String condition = node.getCondition();

        boolean condResult = evaluateCondition(condition, currentProduct);

        if (condResult) {
            if (node.getThenBlock() != null) {
                for (ASTNode child : node.getThenBlock().getChildren()) {
                    sb.append(currentProduct != null
                            ? generateNodeWithData(child, "p", currentProduct)
                            : generateNode(child));
                }
            }
        } else {
            if (node.getElseBlock() != null) {
                for (ASTNode child : node.getElseBlock().getChildren()) {
                    sb.append(currentProduct != null
                            ? generateNodeWithData(child, "p", currentProduct)
                            : generateNode(child));
                }
            }
        }

        return sb.toString();
    }

    // Jinja Expression {{ variable }}
    private String generateJinjaExpr(JinjaExprNode node,
                                     Map<String, String> currentProduct) {
        String varName = node.getVariableName();

        if (currentProduct != null && varName.contains(".")) {
            String[] parts = varName.split("\\.", 2);
            String field = parts[1];
            String value = currentProduct.get(field);
            return value != null ? value : "";
        }

        if (currentProduct != null) {
            String value = currentProduct.get(varName);
            return value != null ? value : varName;
        }

        return "{{ " + varName + " }}";
    }

    // توليد العقد مع حقن بيانات منتج محدد
    private String generateNodeWithData(ASTNode node, String loopVar,
                                        Map<String, String> product) {
        if (node == null) return "";

        if (node instanceof JinjaExprNode) {
            return generateJinjaExpr((JinjaExprNode) node, product);
        }

        if (node instanceof JinjaIfNode) {
            return generateJinjaIfWithData((JinjaIfNode) node, loopVar, product);
        }

        if (node instanceof HtmlElementNode) {
            return generateHtmlElementWithData((HtmlElementNode) node, loopVar, product);
        }

        if (node instanceof HtmlSelfCloseNode) {
            return generateSelfCloseWithData((HtmlSelfCloseNode) node, product);
        }

        if (node instanceof HtmlTextNode) {
            String text = node.toString().trim();
            return text.isEmpty() ? "" : text + "\n";
        }

        if (node instanceof HtmlContentNode) {
            StringBuilder sb = new StringBuilder();
            for (ASTNode child : ((HtmlContentNode) node).getChildren()) {
                sb.append(generateNodeWithData(child, loopVar, product));
            }
            return sb.toString();
        }

        if (node instanceof BlockNode) {
            StringBuilder sb = new StringBuilder();
            for (ASTNode child : ((BlockNode) node).getChildren()) {
                sb.append(generateNodeWithData(child, loopVar, product));
            }
            return sb.toString();
        }

        return generateNode(node);
    }

    private String generateHtmlElementWithData(HtmlElementNode node,
                                               String loopVar,
                                               Map<String, String> product) {
        StringBuilder sb = new StringBuilder();
        String tag = node.getTagName();

        sb.append("<").append(tag);
        for (AttributeNode attr : node.getAttributes()) {
            sb.append(" ").append(attr.getName());
            if (attr.getValue() != null) {
                sb.append("=\"").append(resolveAttrValue(attr.getValue(), product))
                        .append("\"");
            }
        }
        sb.append(">\n");

        if (node.getContent() != null) {
            for (ASTNode child : node.getContent().getChildren()) {
                sb.append(generateNodeWithData(child, loopVar, product));
            }
        }

        sb.append("</").append(tag).append(">\n");
        return sb.toString();
    }

    private String generateSelfCloseWithData(HtmlSelfCloseNode node,
                                             Map<String, String> product) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(node.getTagName());
        for (AttributeNode attr : node.getAttributes()) {
            sb.append(" ").append(attr.getName());
            if (attr.getValue() != null) {
                sb.append("=\"")
                        .append(resolveAttrValue(attr.getValue(), product))
                        .append("\"");
            }
        }
        sb.append(" />\n");
        return sb.toString();
    }

    private String generateJinjaIfWithData(JinjaIfNode node, String loopVar,
                                           Map<String, String> product) {
        boolean condResult = evaluateCondition(node.getCondition(), product);
        StringBuilder sb = new StringBuilder();

        BlockNode block = condResult ? node.getThenBlock() : node.getElseBlock();
        if (block != null) {
            for (ASTNode child : block.getChildren()) {
                sb.append(generateNodeWithData(child, loopVar, product));
            }
        }
        return sb.toString();
    }

    // CSS Generation
    private String generateCss(CssBlockNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("<style>\n");
        for (ASTNode child : node.getChildren()) {
            sb.append(generateCssRule(child));
        }
        sb.append("</style>\n");
        return sb.toString();
    }

    private String generateCssRule(ASTNode node) {
        if (node instanceof SelectorRuleNode) {
            SelectorRuleNode rule = (SelectorRuleNode) node;
            StringBuilder sb = new StringBuilder();

            if (rule instanceof ClassSelectorNode) {
                ClassSelectorNode cls = (ClassSelectorNode) rule;
                sb.append(cls.getClassName());
                if (cls.getPseudoSelector() != null) {
                    sb.append(":").append(cls.getPseudoSelector().getName());
                }
            } else if (rule.getSelectors() instanceof SelectorListNode) {
                SelectorListNode list = (SelectorListNode) rule.getSelectors();
                List<String> names = new ArrayList<>();
                for (SelectorNode sel : list.getSelectors()) {
                    String n = sel.getName();
                    if (sel.getPseudoClass() != null) n += ":" + sel.getPseudoClass();
                    names.add(n);
                }
                sb.append(String.join(", ", names));
            }

            sb.append(" {\n");
            if (rule.getDeclarations() != null) {
                for (var decl : rule.getDeclarations()) {
                    sb.append("  ").append(decl.getProperty()).append(": ");
                    if (decl.getValues() != null) {
                        for (var v : decl.getValues()) sb.append(v.getValue()).append(" ");
                    }
                    sb.append(";\n");
                }
            }
            sb.append("}\n");
            return sb.toString();
        }
        return "";
    }

    // Helper Methods
    private boolean evaluateCondition(String condition,
                                      Map<String, String> product) {
        if (condition == null || product == null) return true;

        if (condition.contains(".")) {
            String field = condition.split("\\.", 2)[1];
            String value = product.get(field);
            return value != null && !value.equals("None") && !value.isEmpty();
        }

        String value = product.get(condition);
        return value != null && !value.equals("None") && !value.isEmpty();
    }

    private String resolveAttrValue(String value, Map<String, String> product) {
        if (value == null || product == null) return value;
        if (product.containsKey(value)) {
            return product.get(value);
        }
        return value;
    }
}