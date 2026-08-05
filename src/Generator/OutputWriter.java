package Generator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;


public class OutputWriter {

    private final Path outputDir;
    private final Path compilerOutputDir;

    public OutputWriter(String outputDirPath, String compilerOutputDirPath) {
        this.outputDir = Paths.get(outputDirPath);
        this.compilerOutputDir = Paths.get(compilerOutputDirPath);
    }

    public void prepareDirectories() throws IOException {
        Files.createDirectories(outputDir);
        Files.createDirectories(compilerOutputDir);
    }

    public void writeGeneratedPages(Map<String, String> pages) throws IOException {
        for (Map.Entry<String, String> e : pages.entrySet()) {
            writeText(outputDir.resolve(e.getKey()), e.getValue());
        }
    }

    public void copySupportFiles(String... sourcePaths) throws IOException {
        for (String src : sourcePaths) {
            Path srcPath = Paths.get(src);
            if (Files.exists(srcPath)) {
                Files.copy(srcPath, outputDir.resolve(srcPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void writeAstPythonJson(String json) throws IOException {
        writeText(compilerOutputDir.resolve("ast_python.json"), json);
    }

    public void writeAstJinjaJson(String json) throws IOException {
        writeText(compilerOutputDir.resolve("ast_jinja.json"), json);
    }

    public void writeSemanticReport(List<String> errors) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (errors == null || errors.isEmpty()) {
            sb.append("No semantic errors found. Semantic Analysis PASSED.\n");
        } else {
            sb.append("Semantic Analysis FAILED with ").append(errors.size()).append(" error(s):\n");
            sb.append("----------------------------------------\n");
            for (String e : errors) sb.append(e).append("\n");
        }
        writeText(compilerOutputDir.resolve("semantic_report.txt"), sb.toString());
    }

    public void writeGenerationLog(List<String> logEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Code Generation Log\n");
        sb.append("====================\n");
        if (logEntries.isEmpty()) {
            sb.append("(no log entries)\n");
        } else {
            for (String entry : logEntries) sb.append(entry).append("\n");
        }
        writeText(compilerOutputDir.resolve("generation_log.txt"), sb.toString());
    }

    private void writeText(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
