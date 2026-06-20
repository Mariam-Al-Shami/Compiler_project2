package Ast.Python;

import Ast.ASTNode;
import Visitor.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

public class PythonImportNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public enum ImportKind { IMPORT_MODULE, FROM_MODULE, FROM_PATH }

    private final ImportKind kind;
    private final String      source;
    private final List<String> names;

    public PythonImportNode(ImportKind kind, String source,
                            List<String> names, int lineNumber) {
        super("PythonImportNode", lineNumber, 0);
        this.kind   = kind;
        this.source = source;
        this.names  = (names != null) ? names : new ArrayList<>();
    }

    public ImportKind  getKind()   { return kind; }
    public String      getSource() { return source; }
    public List<String> getNames() { return names; }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent)
                .append("└── [PythonImportNode]")
                .append("  |  Kind: ").append(kind)
                .append("  |  Source: ").append(source)
                .append("  |  Names: ").append(names)
                .append("  |  Line: ").append(lineNumber).append("\n");
        return sb.toString();
    }
}
