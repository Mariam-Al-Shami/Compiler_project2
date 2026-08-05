import Ast.ProgramNode;
import Generator.*;
import SymbolTable.SymbolTableBuilder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import Visitor.ASTBuilderVisitor;
import Ast.ASTNode;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import antlr.*;
import Ast.SemanticAnalysis.SemanticAnalyzer;
import Ast.SemanticAnalysis.SemanticError;

public class Main {

    // دالة مساعدة لبناء AST من أي ملف
    private static ASTNode buildAST(String fileName) throws IOException {
        CharStream input = CharStreams.fromFileName(fileName);

        exampleLexer lexer = new exampleLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ConsoleErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        exampleParser parser = new exampleParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ConsoleErrorListener.INSTANCE);

        ParseTree tree = parser.program();

        ASTBuilderVisitor visitor = new ASTBuilderVisitor();
        return visitor.visit(tree);
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) { }

        try {
            System.out.println(" اختبار الأخطاء ");
            System.out.println("ملف: tests/errors_test.py");
            runSemanticDemo("tests/errors_test.py");

            System.out.println("\n\n");
            System.out.println(" مرحلة Code Generation");
            runRealPipeline();

        } catch (IOException e) {
            System.err.println("خطأ: لم نتمكن من العثور على الملف أو قراءته.");
            System.err.println(e.getMessage());
        }
    }

    private static void runSemanticDemo(String pythonFile) throws IOException {
        ASTNode pythonAST = buildAST(pythonFile);
        SymbolTableBuilder stBuilder = new SymbolTableBuilder();
        stBuilder.build(pythonAST);

        SemanticAnalyzer analyzer = new SemanticAnalyzer(stBuilder.getGlobalScope());
        pythonAST.accept(analyzer);

        List<SemanticError> errors = new ArrayList<>();
        errors.addAll(stBuilder.getErrors());
        errors.addAll(analyzer.getErrors());

        if (errors.isEmpty()) {
            System.out.println("لا يوجد أخطاء دلالية.");
        } else {
            System.out.println("عدد الأخطاء الدلالية المكتشفة: " + errors.size());
            for (SemanticError err : errors) System.out.println("  - " + err);
        }
    }

    private static void runRealPipeline() throws IOException {
        String pythonFile = "flask_app/app.py";
        String templatesDir = "flask_app/templates";
        String outputDir = "output";
        String compilerOutputDir = "compiler_output";

        OutputWriter writer = new OutputWriter(outputDir, compilerOutputDir);
        writer.prepareDirectories();

        // المرحلة 1: Python AST
        System.out.println("\nبناء Python AST من " + pythonFile);
        ASTNode pythonAST = buildAST(pythonFile);

        // طباعة شجرة الـ AST (Python)
        System.out.println("\n شجرة AST (Python)");
        System.out.println(pythonAST.print(""));
        System.out.println("= نهاية شجرة AST\n");

        // المرحلة 2 + 3: Symbol Table + Semantic Analysis
        System.out.println("بناء Symbol Table وتشغيل Semantic Analysis");
        SymbolTableBuilder stBuilder = new SymbolTableBuilder();
        stBuilder.build(pythonAST);

        // طباعة Symbol Table (كل الـ scopes بشكل متسلسل)
        System.out.println("\n Symbol Table");
        stBuilder.getGlobalScope().printTableRecursive();
        System.out.println("نهاية Symbol Table \n");

        SemanticAnalyzer analyzer = new SemanticAnalyzer(stBuilder.getGlobalScope());
        pythonAST.accept(analyzer);

        List<SemanticError> errors = new ArrayList<>();
        errors.addAll(stBuilder.getErrors());
        errors.addAll(analyzer.getErrors());

        // نكتب دائمًا semantic_report.txt (سواء نجح أو فشل)
        List<String> errorStrings = new ArrayList<>();
        for (SemanticError e : errors) errorStrings.add(e.toString());
        writer.writeSemanticReport(errorStrings);

        // نكتب ast_python.json دائمًا (متاح بعد التحليل بغض النظر عن نتيجة السيمانتيك)
        AstJsonSerializer jsonSerializer = new AstJsonSerializer();
        writer.writeAstPythonJson(jsonSerializer.toJson(pythonAST));

        if (!errors.isEmpty()) {
            System.out.println("\n توقفت مرحلة Code Generation: يوجد " + errors.size()
                    + " خطأ دلالي في app.py. راجع compiler_output/semantic_report.txt");
            for (SemanticError e : errors) System.out.println("  - " + e);
            List<String> log = new ArrayList<>();
            log.add("[Generator] Generation aborted: Semantic Analysis failed with " + errors.size() + " error(s).");
            log.add("[Generator] Fix errors in semantic_report.txt and re-run.");
            writer.writeGenerationLog(log);
            return;
        }
        System.out.println(" لا أخطاء دلالية — الانتقال لمرحلة Code Generation");

        //  المرحلة 4: بناء GenerationContext
        System.out.println("\n GenerationContext (استخراج المنتجات + الـ Routes)");
        GenerationContext ctx = new GenerationContext();
        PythonDataExtractor extractor = new PythonDataExtractor();

        Map<String, String> routes = extractor.extractRoutes(pythonAST, ctx);
        ctx.setRoutes(routes);

        List<Map<String, String>> persistedProducts =
                extractor.loadPersistedProducts("flask_app/products.json", ctx);

        if (!persistedProducts.isEmpty()) {
            ctx.setProducts(persistedProducts);
        } else {
            List<Map<String, String>> staticProducts = extractor.extractStaticProducts(pythonAST, ctx);
            if (staticProducts.isEmpty()) {
                ctx.setProducts(extractor.getSampleProducts());
                ctx.setUsingMockData(true);
                ctx.addLog("[ContextBuilder] Using MOCK/DEMO product data (see warning above) — clearly documented assumption.");
            } else {
                ctx.setProducts(staticProducts);
            }
        }

        // ---------- المرحلة 5 + 6: لكل ملف داخل templates/ -> Jinja AST -> HTML
        System.out.println("معالجة كل قوالب " + templatesDir);
        File dir = new File(templatesDir);
        File[] templateFiles = dir.listFiles((d, name) -> name.endsWith(".html") || name.endsWith(".jinja"));
        if (templateFiles == null) templateFiles = new File[0];
        Arrays.sort(templateFiles, Comparator.comparing(File::getName));

        Map<String, ASTNode> jinjaAsts = new LinkedHashMap<>();
        Map<String, String> generatedPages = new LinkedHashMap<>();

        for (File tf : templateFiles) {
            String name = tf.getName();
            String routeName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            System.out.println("    - " + name);
            ASTNode jinjaAST = buildAST(tf.getPath());
            jinjaAsts.put(name, jinjaAST);

            JinjaGenerator generator = new JinjaGenerator(ctx);
            String routePattern = routes.get(routeName);

            if (routePattern != null && routePattern.contains("<") && !ctx.getProducts().isEmpty()) {
                for (Map<String, String> p : ctx.getProducts()) {
                    String id = p.getOrDefault("id", "0");
                    String html = generator.generate(jinjaAST, p);
                    String outName = routeName + "_" + id + ".html";
                    generatedPages.put(outName, html);
                    ctx.addLog("[Generator] Generated " + outName + " for product id=" + id + " (" + html.length() + " chars).");
                }
            } else {
                String html = generator.generate(jinjaAST);
                generatedPages.put(name, html);
                ctx.addLog("[Generator] Generated " + name + " (" + html.length() + " chars).");
            }
        }
        ctx.addOutput("*", "done");

        //  كتابة ast_jinja.json
        writer.writeAstJinjaJson(jsonSerializer.toJson(jinjaAsts));

        //  كتابة output
        System.out.println(" كتابة الملفات الناتجة في " + outputDir + "/ و " + compilerOutputDir + "/");
        writer.writeGeneratedPages(generatedPages);
        writer.copySupportFiles(pythonFile, "flask_app/style.css", "flask_app/script.js");

        // كتابة generation_log.txt
        writer.writeGenerationLog(ctx.getLogEntries());

        System.out.println("\nاكتملت مرحلة Code Generation");
        System.out.println("   عدد الصفحات المولَّدة: " + generatedPages.size());
        System.out.println("   عدد التحذيرات (Generation Warnings): " + ctx.getWarnings().size());
    }
}