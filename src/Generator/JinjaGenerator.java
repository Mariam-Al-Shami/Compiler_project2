package Generator;

import Ast.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Css.*;
import java.util.*;
import java.util.regex.*;

/**
 * JinjaGenerator
 * --------------
 * يحوّل Jinja/HTML AST إلى HTML نهائي، معتمدًا على GenerationContext
 * (المنتجات + الـ Routes) لحل {{ }} و{% for %} و{% if %} وكذلك تعابير
 * Jinja داخل قيم الـ attributes (href/src) مثل url_for() و '~' concatenation.
 *
 * قواعد الأخطاء (Generation Warnings فقط - وليست Semantic Errors):
 * أي متغير أو route أو وسم غير مدعوم -> يُسجَّل Warning في GenerationContext
 * ويُكتب نص فارغ / "#" بدل الانهيار.
 */
public class JinjaGenerator {

    private static final Pattern JINJA_EXPR = Pattern.compile("\\{\\{\\s*(.*?)\\s*\\}\\}");
    private static final Pattern CALL_EXPR  = Pattern.compile("^(\\w+)\\((.*)\\)$", Pattern.DOTALL);

    private final GenerationContext ctx;

    public JinjaGenerator(GenerationContext ctx) {
        this.ctx = ctx;
    }

    public String generate(ASTNode root) {
        return generate(root, null);
    }

    // =========================================================
    // توليد الصفحة كاملةً "لأجل منتج محدد" (مثلاً product_details.html
    // و delete_product.html يلي بتعتمد عـ {{ product.name }} مباشرة
    // بدون ما تكون جوا {% for %}). قبل هيك كانت هالصفحات دايمًا بتطلع
    // فاضية لأنه ما في "product" بالـ context خارج حلقة الـ for.
    // =========================================================
    public String generate(ASTNode root, Map<String, String> product) {
        StringBuilder sb = new StringBuilder();
        if (root instanceof ProgramNode) {
            for (ASTNode stmt : ((ProgramNode) root).getStatements()) {
                sb.append(generateNodeWithData(stmt, null, product));
            }
        } else {
            ctx.addWarning("Generation root is not a valid Jinja/HTML AST (ProgramNode expected).");
        }
        return sb.toString();
    }

    // =========================================================
    // توليد بدون سياق منتج (خارج أي for loop)
    // =========================================================
    private String generateNode(ASTNode node) {
        return generateNodeWithData(node, null, null);
    }

    // =========================================================
    // توليد عام (مع/بدون سياق منتج حسب product)
    // =========================================================
    private String generateNodeWithData(ASTNode node, String loopVar, Map<String, String> product) {
        if (node == null) return "";

        if (node instanceof DoctypeNode) {
            return "<!DOCTYPE html>\n";
        }
        if (node instanceof HtmlElementNode) {
            return generateHtmlElement((HtmlElementNode) node, loopVar, product);
        }
        if (node instanceof HtmlSelfCloseNode) {
            return generateSelfClose((HtmlSelfCloseNode) node, loopVar, product);
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
        if (node instanceof HtmlBlockNode) {
            HtmlBlockNode block = (HtmlBlockNode) node;
            StringBuilder sb = new StringBuilder();
            if (block.getOpening() != null) sb.append(generateNodeWithData(block.getOpening(), loopVar, product));
            if (block.getContent() != null) sb.append(generateNodeWithData(block.getContent(), loopVar, product));
            if (block.getClosing() != null) sb.append(generateNodeWithData(block.getClosing(), loopVar, product));
            return sb.toString();
        }
        if (node instanceof JinjaForNode) {
            return generateJinjaFor((JinjaForNode) node);
        }
        if (node instanceof JinjaIfNode) {
            return generateJinjaIf((JinjaIfNode) node, loopVar, product);
        }
        if (node instanceof JinjaExprNode) {
            return resolveExpression(((JinjaExprNode) node).getVariableName(), loopVar, product);
        }
        if (node instanceof CssBlockNode) {
            return generateCss((CssBlockNode) node);
        }
        if (node instanceof BlockNode) {
            StringBuilder sb = new StringBuilder();
            for (ASTNode child : ((BlockNode) node).getChildren()) {
                sb.append(generateNodeWithData(child, loopVar, product));
            }
            return sb.toString();
        }

        ctx.addWarning("Unknown/unsupported node type during generation: " + node.getClass().getSimpleName());
        return "";
    }

    // HTML Element (مع دعم Jinja داخل الـ attributes)
    private String generateHtmlElement(HtmlElementNode node, String loopVar, Map<String, String> product) {
        StringBuilder sb = new StringBuilder();
        String tag = node.getTagName();
        sb.append("<").append(tag);
        for (AttributeNode attr : node.getAttributes()) {
            sb.append(" ").append(attr.getName());
            if (attr.getValue() != null) {
                sb.append("=\"").append(resolveAttributeValue(attr.getValue(), loopVar, product)).append("\"");
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

    private String generateSelfClose(HtmlSelfCloseNode node, String loopVar, Map<String, String> product) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(node.getTagName());
        for (AttributeNode attr : node.getAttributes()) {
            sb.append(" ").append(attr.getName());
            if (attr.getValue() != null) {
                sb.append("=\"").append(resolveAttributeValue(attr.getValue(), loopVar, product)).append("\"");
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

        sb.append("<!-- Jinja For: for ").append(loopVar).append(" in ").append(iterable).append(" -->\n");

        List<Map<String, String>> products = ctx.getProducts();
        if (products == null || products.isEmpty()) {
            ctx.addLog("[JinjaRenderer] Loop over '" + iterable + "' skipped: collection is empty.");
            sb.append("<!-- لا توجد بيانات لعرضها -->\n");
            sb.append("<!-- endfor -->\n");
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

    // Jinja If / Else  (مبدأ: لا نولّد العنصر إن كان الشرط غير محقق - if-based generation)
    private String generateJinjaIf(JinjaIfNode node, String loopVar, Map<String, String> product) {
        StringBuilder sb = new StringBuilder();
        boolean condResult = evaluateCondition(node.getCondition(), product);

        BlockNode block = condResult ? node.getThenBlock() : node.getElseBlock();
        if (block != null) {
            for (ASTNode child : block.getChildren()) {
                sb.append(generateNodeWithData(child, loopVar, product));
            }
        }
        return sb.toString();
    }

    // =========================================================
    // حل تعابير {{ ... }} داخل قيم attribute (href/src) أو كنص مستقل
    // =========================================================
    private String resolveAttributeValue(String rawValue, String loopVar, Map<String, String> product) {
        if (rawValue == null) return "";
        Matcher m = JINJA_EXPR.matcher(rawValue);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            out.append(rawValue, last, m.start());
            out.append(resolveExpression(m.group(1), loopVar, product));
            last = m.end();
        }
        out.append(rawValue.substring(last));
        return out.toString();
    }

    private String resolveExpression(String expr, String loopVar, Map<String, String> product) {
        if (expr == null) return "";
        expr = expr.trim();

        Matcher callM = CALL_EXPR.matcher(expr);
        if (callM.matches()) {
            String funcName = callM.group(1);
            String argsStr  = callM.group(2);
            if (funcName.equals("url_for")) {
                return resolveUrlFor(argsStr, loopVar, product);
            }
            ctx.addWarning("Unsupported function call during generation: " + funcName + "(...)");
            return "";
        }

        if (expr.contains("~")) {
            String[] parts = expr.split("~");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) sb.append(resolveSimpleValue(part.trim(), loopVar, product));
            return sb.toString();
        }

        return resolveSimpleValue(expr, loopVar, product);
    }

    private String resolveSimpleValue(String token, String loopVar, Map<String, String> product) {
        if (token.length() >= 2 &&
                ((token.charAt(0) == '\'' && token.charAt(token.length() - 1) == '\'') ||
                        (token.charAt(0) == '"'  && token.charAt(token.length() - 1) == '"'))) {
            return token.substring(1, token.length() - 1); // literal string
        }

        if (product != null) {
            String field = token;
            if (token.contains(".")) {
                String[] p = token.split("\\.", 2);
                field = p[1];
            }
            String value = product.get(field);
            if (value != null) return value;
        }

        ctx.addWarning("Variable '" + token + "' not found in GenerationContext");
        return "";
    }

    private String resolveUrlFor(String argsStr, String loopVar, Map<String, String> product) {
        List<String> args = splitArgs(argsStr);
        if (args.isEmpty()) {
            ctx.addWarning("url_for() called with no arguments");
            return "#";
        }

        String routeName = stripQuotes(args.get(0).trim());
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 1; i < args.size(); i++) {
            String[] kv = args.get(i).split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0].trim(), resolveExpression(kv[1].trim(), loopVar, product));
            }
        }

        // ملاحظة تعديل: بما إنه صفحات الخرج static HTML رح تنفتح مباشرة
        // بالمتصفح (file://) بدون سيرفر Flask شغال، ما بينفع نرجع مسار
        // Flask المطلق (زي /product/3) لأنه رح يفشل. بدل هيك، منولد رابط
        // نسبي بيشاور عمِلف الـ HTML الفعلي المولَّد بمجلد output/.
        if (routeName.equals("static")) {
            // بمجلد output/ الملفات الثابتة منسوخة تحت static/ (مسار نسبي)
            return "static/" + params.getOrDefault("filename", "");
        }

        if (!ctx.getRoutes().containsKey(routeName)) {
            ctx.addWarning("Route '" + routeName + "' not found for url_for() call");
            return "#";
        }

        // اسم ملف HTML المولَّد = نفس اسم دالة الـ route (متل اسم قالب Jinja)
        String fileName = routeName + ".html";

        if (params.isEmpty()) {
            return fileName;
        }

        // إذا الـ route فيه بارامتر بالـ pattern تبعو (زي <int:product_id>)
        // فهاد يعني هي صفحة "لكل منتج" وبتتولد نسخة منفصلة إلها لكل منتج
        // بالاسم route_<id>.html (شوفي Main.java مرحلة التوليد). فمنولد
        // رابط عالملف الصحيح مباشرة بدل query string ما حدا عم يقرأه.
        String pattern = ctx.getRoutes().get(routeName);
        if (pattern != null && pattern.contains("<")) {
            String idValue = params.values().iterator().next();
            return routeName + "_" + idValue + ".html";
        }

        // إذا route_pattern ما فيه بارامتر بس استدعاء url_for جالو args
        // زيادة (حالة نادرة) منمررها كـ query string احتياطيًا
        // نسبي عالملف نفسه، لحد ما نفعّل توليد صفحة منفصلة لكل منتج.
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (query.length() > 0) query.append("&");
            query.append(e.getKey()).append("=").append(e.getValue());
        }
        return fileName + "?" + query;
    }

    private List<String> splitArgs(String argsStr) {
        List<String> result = new ArrayList<>();
        if (argsStr == null || argsStr.trim().isEmpty()) return result;
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        for (char c : argsStr.toCharArray()) {
            if (inQuotes) {
                cur.append(c);
                if (c == quoteChar) inQuotes = false;
            } else if (c == '\'' || c == '"') {
                inQuotes = true;
                quoteChar = c;
                cur.append(c);
            } else if (c == ',') {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) result.add(cur.toString());
        return result;
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() >= 2 && (s.charAt(0) == '\'' || s.charAt(0) == '"')) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    // =========================================================
    // شرط {% if %} (مبدأ if-based generation)
    // =========================================================
    private boolean evaluateCondition(String condition, Map<String, String> product) {
        if (condition == null) return true;
        if (product == null) {
            ctx.addWarning("Condition '" + condition + "' could not be evaluated (no product in scope); treated as false.");
            return false;
        }
        String field = condition.contains(".") ? condition.split("\\.", 2)[1] : condition;
        String value = product.get(field);
        return value != null && !value.equals("None") && !value.equals("null") && !value.isEmpty();
    }

    // =========================================================
    // CSS
    // =========================================================
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
}