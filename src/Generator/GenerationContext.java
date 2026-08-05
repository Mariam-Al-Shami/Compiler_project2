package Generator;

import java.util.*;

public class GenerationContext {

    private List<Map<String, String>> products = new ArrayList<>();  //منستخرجها من فلاسك
    private Map<String, String> routes = new LinkedHashMap<>();
    private Map<String, String> outputHtml = new LinkedHashMap<>();
    private List<String> logEntries = new ArrayList<>(); //كلشي صار وقت التوليد
    private List<String> warnings = new ArrayList<>();
    private boolean usingMockData = false; //true اذا ما لقينا منتجات حقيقية (لا من products.json ولا من AST) واستخدمنا بيانات تجريبية

    public List<Map<String, String>> getProducts() { return products; }
    public void setProducts(List<Map<String, String>> products) { this.products = products; }

    public Map<String, String> getRoutes() { return routes; }
    public void setRoutes(Map<String, String> routes) { this.routes = routes; }

    public Map<String, String> getOutputHtml() { return outputHtml; }
    public void addOutput(String pageName, String html) { outputHtml.put(pageName, html); }

    public void addLog(String entry) { logEntries.add(entry); }
    public List<String> getLogEntries() { return logEntries; }

    public void addWarning(String warning) {
        warnings.add(warning);
        logEntries.add("[WARNING] " + warning);
    }
    public List<String> getWarnings() { return warnings; }

    public boolean isUsingMockData() { return usingMockData; }
    public void setUsingMockData(boolean usingMockData) { this.usingMockData = usingMockData; }

}