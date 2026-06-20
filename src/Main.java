import Ast.ProgramNode;
import Generator.JinjaGenerator;
import Generator.PythonDataExtractor;
import SymbolTable.SymbolTableBuilder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import Visitor.ASTBuilderVisitor;
import Ast.ASTNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            // المرحلة 1: بناء شجرة Python AST
            System.out.println("----------------------------------------");
            System.out.println(" المرحلة 1: بناء شجرة Python AST");
            System.out.println("----------------------------------------");

//            String pythonFile = "flask_app/app.py";
            String pythonFile = "tests/errors_test.py";
            ASTNode pythonAST = buildAST(pythonFile);

            System.out.println("\n Python AST Tree \n");
            if (pythonAST != null) {
                ((ProgramNode) pythonAST).printDetailedTree();
            }
            // المرحلة 2: بناء جدول الرموز (Symbol Table)
            System.out.println("\n----------------------------------------");
            System.out.println(" المرحلة 2: بناء جدول الرموز (Symbol Table)");
            System.out.println("----------------------------------------");

            SymbolTableBuilder stBuilder = new SymbolTableBuilder();
            stBuilder.build(pythonAST);

            System.out.println("\n--- Symbol Table (كل النطاقات) ---");
            stBuilder.getGlobalScope().printTableRecursive();

            // المرحلة 3: التحليل الدلالي (Semantic Analysis)
            System.out.println("\n----------------------------------------");
            System.out.println(" المرحلة 3: التحليل الدلالي (Semantic Analysis)");
            System.out.println("----------------------------------------");

            SemanticAnalyzer analyzer = new SemanticAnalyzer(stBuilder.getGlobalScope());
            pythonAST.accept(analyzer);

            List<SemanticError> errors = new ArrayList<>();
            errors.addAll(stBuilder.getErrors());
            errors.addAll(analyzer.getErrors());

            if (errors.isEmpty()) {
                System.out.println("\n✔ لا يوجد أخطاء دلالية.");
            } else {
                System.out.println("\nعدد الأخطاء الدلالية: " + errors.size());
                System.out.println("----------------------------------------");
                for (SemanticError err : errors) {
                    System.out.println(err);
                }
                System.out.println("----------------------------------------");
            }

            // المرحلة 4: استخراج البيانات من Python
            System.out.println("\n----------------------------------------");
            System.out.println(" المرحلة 4: استخراج البيانات من Python AST");
            System.out.println("----------------------------------------");

            PythonDataExtractor extractor = new PythonDataExtractor();
            List<Map<String, String>> products = extractor.getSampleProducts();

            System.out.println("عدد المنتجات: " + products.size());
            for (int i = 0; i < products.size(); i++) {
                System.out.println("  منتج " + (i+1) + ": " + products.get(i));
            }

            // المرحلة 5: بناء شجرة Jinja AST
            System.out.println("\n----------------------------------------");
            System.out.println(" المرحلة 5: بناء شجرة Jinja AST");
            System.out.println("----------------------------------------");

            String jinjaFile = "flask_app/templates/products.html";
            ASTNode jinjaAST = buildAST(jinjaFile);

            System.out.println("\n Jinja AST Tree \n");
            if (jinjaAST != null) {
                ((ProgramNode) jinjaAST).printDetailedTree();
            }

            // المرحلة 6: تشغيل الـ Generator
            System.out.println("\n----------------------------------------");
            System.out.println(" المرحلة 6: توليد HTML من Jinja AST + Python Data");
            System.out.println("----------------------------------------");

            JinjaGenerator generator = new JinjaGenerator(products);
            String generatedHtml = generator.generate(jinjaAST);

            System.out.println("\n--- HTML الناتج ---\n");
            System.out.println(generatedHtml);

            System.out.println("\n----------------------------------------");
            System.out.println(" AST Processed Successfully ");
            System.out.println("----------------------------------------");

        } catch (IOException e) {
            System.err.println("خطأ: لم نتمكن من العثور على الملف أو قراءته.");
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("حدث خطأ أثناء عملية الـ Parsing:");
            e.printStackTrace();
        }
    }
}