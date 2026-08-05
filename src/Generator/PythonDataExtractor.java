package Generator;

import Ast.*;
import Ast.Python.*;
import java.util.*;


//بيقرا من ast بايثون وبيستخرج منها : منجات  و راوتات
public class PythonDataExtractor {

    //بيرجع ليست كل عنصر فيها هو منتج ممثل متل خريطة
    public List<Map<String, String>> extractStaticProducts(ASTNode root, GenerationContext ctx) {
        //بيدور جوا ast عن بيانات منتجات ماتكون جوا الدوال
        List<Map<String, String>> products = new ArrayList<>();
        //اذا الجذر مو من شجرة programNode رجع الليست
        if (!(root instanceof ProgramNode))
            return products;

        Map<String, String> currentProduct = null;

        //حلقة بتمشي على كل جملة برمجية موجودة بالكود
        for (ASTNode stmt : ((ProgramNode) root).getStatements()) {

            //كل مايشوف products_data.append  بيعتبر انو المنتج خلص وبيضيفو لليست
            if (stmt instanceof PythonCallNode) {
                PythonCallNode call = (PythonCallNode) stmt; //بيشوف اذاالجملة هي استدعاء لدالة تانية
                //هل الدالة الي استدعيناها  هي roducts_data_append
                if (call.getCallee() != null && call.getCallee().equals("products_data.append")) {
                    if (currentProduct != null && !currentProduct.isEmpty()) {
                        products.add(new LinkedHashMap<>(currentProduct)); //منضيف نسخة من المنتج يلي لقيناه لليست ومامنعدل عالقيمةفورا منعمل نسخة ومنحفظها
                        currentProduct = new LinkedHashMap<>(); //بعد ضفنا المنتج لليست منصفر القيمة ومنعيد العملية
                    }
                }
            }

            //هل الجملة هي عملية تعيين او اسناد
            if (stmt instanceof PythonAssignNode) {
                PythonAssignNode assign = (PythonAssignNode) stmt;
                String target = assign.getTarget();

                //بيدور على new_product بكل statement بال programNode
                if (target != null && target.startsWith("new_product")) {
                    if (currentProduct == null) currentProduct = new LinkedHashMap<>();
                    String key = extractKey(target);
                    String value = extractValue(assign.getValue());
                    if (key != null && value != null) currentProduct.put(key, value);
                }
                if (target != null && target.equals("new_product")) {
                    currentProduct = new LinkedHashMap<>();
                }
            }
        }

        //المنتجات بتنبنى ديناميك وقت تشغل مو ثابت جوا الكود
        if (products.isEmpty()) {
            ctx.addWarning("No static product data found at module level in app.py " +
                    "(products_data is populated dynamically at runtime via POST requests). " +
                    "Falling back to demo/mock data for generation output.");
        } else {
            ctx.addLog("[ContextBuilder] Extracted " + products.size() + " static product(s) from Python AST.");
        }

        return products;
    }

    private String extractKey(String target) {
        if (target.contains("'")) {
            int start = target.indexOf("'") + 1;
            int end = target.lastIndexOf("'");
            if (start < end) return target.substring(start, end);
        }
        return null;
    }

    private String extractValue(ASTNode valueNode) {
        if (valueNode == null) return "N/A";
        if (valueNode instanceof PythonExprNode) {
            PythonExprNode expr = (PythonExprNode) valueNode;
            if (expr.getRawText() != null) return expr.getRawText();
        }
        if (valueNode instanceof PythonCallNode) {
            PythonCallNode call = (PythonCallNode) valueNode;
            return call.getCallee() + "(" + call.getArgs() + ")";
        }
        return "N/A";
    }

    public java.util.List<Map<String, String>> loadPersistedProducts(String path, GenerationContext ctx) {
        java.util.List<Map<String, String>> result = new ArrayList<>();
        java.io.File file = new java.io.File(path);
        if (!file.exists()) return result;

        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            result = parseFlatJsonArray(content);
            if (!result.isEmpty()) {
                ctx.addLog("[ContextBuilder] Loaded " + result.size()
                        + " REAL product(s) from live runtime snapshot: " + path);
            }
        } catch (java.io.IOException e) {
            ctx.addWarning("Failed to read product snapshot file '" + path + "': " + e.getMessage());
        }
        return result;
    }

    private java.util.List<Map<String, String>> parseFlatJsonArray(String json) {
        java.util.List<Map<String, String>> list = new ArrayList<>();
        int i = 0;
        int n = json.length();
        while (i < n && json.charAt(i) != '[') i++;
        i++;
        while (i < n) {
            while (i < n && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= n || json.charAt(i) == ']') break;
            if (json.charAt(i) == '{') {
                int[] pos = {i};
                Map<String, String> obj = parseFlatJsonObject(json, pos);
                list.add(obj);
                i = pos[0];
            } else {
                i++;
            }
            while (i < n && json.charAt(i) != ',' && json.charAt(i) != ']') i++;
            if (i < n && json.charAt(i) == ',') i++;
        }
        return list;
    }

    private Map<String, String> parseFlatJsonObject(String json, int[] pos) {
        Map<String, String> obj = new LinkedHashMap<>();
        int i = pos[0] + 1;
        int n = json.length();
        while (i < n) {
            while (i < n && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= n || json.charAt(i) == '}') {
                i++;
                break;
            }
            String key = null;
            if (json.charAt(i) == '"') {
                int start = ++i;
                while (i < n && json.charAt(i) != '"') i++;
                key = json.substring(start, i);
                i++;
            }
            while (i < n && json.charAt(i) != ':') i++;
            i++;
            while (i < n && Character.isWhitespace(json.charAt(i))) i++;
            String value;
            if (i < n && json.charAt(i) == '"') {
                int start = ++i;
                StringBuilder sb = new StringBuilder();
                while (i < n && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\' && i + 1 < n) {
                        sb.append(json.charAt(i + 1));
                        i += 2;
                    } else {
                        sb.append(json.charAt(i));
                        i++;
                    }
                }
                value = sb.toString();
                i++;
            } else {
                int start = i;
                while (i < n && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                value = json.substring(start, i).trim();
            }
            if (key != null) obj.put(key, value);
            while (i < n && Character.isWhitespace(json.charAt(i))) i++;
            if (i < n && json.charAt(i) == ',') {
                i++;
                continue;
            }
            if (i < n && json.charAt(i) == '}') {
                i++;
                break;
            }
        }
        pos[0] = i;
        return obj;
    }

    //بيانات تجريبية/احتياطية (Mock) بتستخدم فقط إذا ما لقينا ولا منتج
    //حقيقي لا من flask_app/products.json ولا من الـ Python AST
    public List<Map<String, String>> getSampleProducts() {
        List<Map<String, String>> sample = new ArrayList<>();

        Map<String, String> p1 = new LinkedHashMap<>();
        p1.put("id", "0");
        p1.put("name", "Demo Product 1");
        p1.put("price", "0");
        p1.put("details", "Sample/mock data - no real product data found.");
        sample.add(p1);

        Map<String, String> p2 = new LinkedHashMap<>();
        p2.put("id", "1");
        p2.put("name", "Demo Product 2");
        p2.put("price", "0");
        p2.put("details", "Sample/mock data - no real product data found.");
        sample.add(p2);

        return sample;
    }

    //بيستخرج راوتات الفلاسك
    public Map<String, String> extractRoutes(ASTNode root, GenerationContext ctx) {
        Map<String, String> routes = new LinkedHashMap<>();
        if (!(root instanceof ProgramNode)) return routes;

        List<ASTNode> statements = ((ProgramNode) root).getStatements();
        for (int i = 0; i < statements.size(); i++) {
            ASTNode stmt = statements.get(i);
            if (stmt instanceof PythonDecoratorNode) {
                PythonDecoratorNode dec = (PythonDecoratorNode) stmt;
                if (dec.getPath() != null && dec.getPath().equals("app.route")) {
                    // أول argument هو نمط الـ URL
                    if (!dec.getArguments().isEmpty()) {
                        String pattern = stripQuotes(dec.getArguments().get(0));
                        // الدالة المرتبطة هي أقرب PythonFunctionNode بعد الديكوريتر
                        for (int j = i + 1; j < statements.size(); j++) {
                            if (statements.get(j) instanceof PythonFunctionNode) {
                                String funcName = ((PythonFunctionNode) statements.get(j)).getName();
                                routes.put(funcName, pattern);
                                ctx.addLog("[ContextBuilder] Route extracted: " + funcName + " -> " + pattern);
                                break;
                            }
                        }
                    }
                }
            }
        }

        routes.put("static", "/static/<filename>");
        return routes;
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() >= 2 && (s.charAt(0) == '\'' || s.charAt(0) == '"')) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

}