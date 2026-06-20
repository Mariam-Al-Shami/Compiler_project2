package Generator;

import Ast.*;
import Ast.Python.*;
import java.util.*;

public class PythonDataExtractor {

    private List<Map<String, String>> products = new ArrayList<>();
    private Map<String, String> currentProduct = null;

    public List<Map<String, String>> extractProducts(ASTNode root) {
        if (root instanceof ProgramNode) {
            walkProgram((ProgramNode) root);
        }
        return products;
    }

    private void walkProgram(ProgramNode program) {
        for (ASTNode stmt : program.getStatements()) {
            walkNode(stmt);
        }
    }

    private void walkNode(ASTNode node) {
        if (node == null) return;

        if (node instanceof PythonCallNode) {
            PythonCallNode call = (PythonCallNode) node;
            if (call.getCallee() != null &&
                    call.getCallee().equals("products_data.append")) {
                if (currentProduct != null && !currentProduct.isEmpty()) {
                    products.add(new LinkedHashMap<>(currentProduct));
                    currentProduct = new LinkedHashMap<>();
                }
            }
        }
        if (node instanceof PythonAssignNode) {
            PythonAssignNode assign = (PythonAssignNode) node;
            String target = assign.getTarget();
            if (target != null && target.startsWith("new_product")) {
                if (currentProduct == null) {
                    currentProduct = new LinkedHashMap<>();
                }
                String key = extractKey(target);
                String value = extractValue(assign.getValue());

                if (key != null && value != null) {
                    currentProduct.put(key, value);
                }
            }
            if (target != null && target.equals("new_product")) {
                currentProduct = new LinkedHashMap<>();
            }
        }
        if (node instanceof PythonFunctionNode) {
            PythonFunctionNode func = (PythonFunctionNode) node;
            if (func.getBody() != null) {
                walkBlock(func.getBody());
            }
        }
        if (node instanceof PythonIfNode) {
            PythonIfNode ifNode = (PythonIfNode) node;
            if (ifNode.getThenBlock() != null) {
                walkBlock(ifNode.getThenBlock());
            }
            if (ifNode.getElseBlock() != null) {
                walkBlock(ifNode.getElseBlock());
            }
        }
    }

    private void walkBlock(BlockNode block) {
        if (block == null) return;
        for (ASTNode child : block.getChildren()) {
            walkNode(child);
        }
    }

    private String extractKey(String target) {
        if (target.contains("'")) {
            int start = target.indexOf("'") + 1;
            int end   = target.lastIndexOf("'");
            if (start < end) {
                return target.substring(start, end);
            }
        }
        return null;
    }

    private String extractValue(ASTNode valueNode) {
        if (valueNode == null) return "N/A";

        if (valueNode instanceof PythonExprNode) {
            PythonExprNode expr = (PythonExprNode) valueNode;
            if (expr.getRawText() != null) {
                return expr.getRawText();
            }
        }

        if (valueNode instanceof PythonCallNode) {
            PythonCallNode call = (PythonCallNode) valueNode;
            return call.getCallee() + "(" + call.getArgs() + ")";
        }

        return "N/A";
    }

    public List<Map<String, String>> getSampleProducts() {
        List<Map<String, String>> sample = new ArrayList<>();

        Map<String, String> p1 = new LinkedHashMap<>();
        p1.put("id", "0");
        p1.put("name", "لابتوب Dell");
        p1.put("price", "1500");
        p1.put("details", "لابتوب قوي للبرمجة");
        p1.put("image", "None");
        sample.add(p1);

        Map<String, String> p2 = new LinkedHashMap<>();
        p2.put("id", "1");
        p2.put("name", "ماوس Logitech");
        p2.put("price", "50");
        p2.put("details", "ماوس لاسلكي");
        p2.put("image", "None");
        sample.add(p2);

        Map<String, String> p3 = new LinkedHashMap<>();
        p3.put("id", "2");
        p3.put("name", "كيبورد Mechanical");
        p3.put("price", "200");
        p3.put("details", "كيبورد ميكانيكي RGB");
        p3.put("image", "None");
        sample.add(p3);

        return sample;
    }
}