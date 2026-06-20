package SymbolTable;

import Ast.*;
import Ast.Css.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Python.*;
import Ast.SemanticAnalysis.DuplicateVariableError;
import Ast.SemanticAnalysis.SemanticError;
import Visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class SymbolTableBuilder implements ASTVisitor {

    private ScopedSymbolTable currentScope;
    private final ScopedSymbolTable globalScope;

    private final List<SemanticError> errors = new ArrayList<>();

    public SymbolTableBuilder() {
        this.globalScope = new ScopedSymbolTable("Global", 0, null);
        this.currentScope = globalScope;
    }

    public void build(ASTNode root) {
        root.accept(this);
    }

    public List<SemanticError> getErrors() {
        return errors;
    }
    private void enterScope(String name) {
        currentScope = currentScope.createChildScope(name);
    }

    private void exitScope() {
        currentScope = currentScope.getParent();
    }

    private void defineWithDuplicateCheck(String name, SymbolEntry entry) {
        if (currentScope.lookupCurrentScopeOnly(name) != null) {
            errors.add(new DuplicateVariableError(name, entry.getLineNumber()));
            return;
        }
        currentScope.define(name, entry);
    }

    // Program Node
    @Override
    public void visit(ProgramNode node) {
        for (ASTNode child : node.getStatements()) {
            child.accept(this);
        }
    }

    // Python Nodes
    @Override
    public void visit(PythonFunctionNode node) {
        SymbolEntry funcEntry = new SymbolEntry(
                node.getName(),
                SymbolEntry.SymbolType.PYTHON_FUNCTION,
                node.getLineNumber()
        );
        defineWithDuplicateCheck(node.getName(), funcEntry);

        enterScope("Function_" + node.getName());
        for (String param : node.getParams()) {
            SymbolEntry paramEntry = new SymbolEntry(
                    param,
                    SymbolEntry.SymbolType.PYTHON_PARAM,
                    node.getLineNumber()
            );
            defineWithDuplicateCheck(param, paramEntry);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        exitScope();
    }

    @Override
    public void visit(PythonAssignNode node) {
        SymbolEntry entry = new SymbolEntry(
                node.getTarget(),
                SymbolEntry.SymbolType.PYTHON_VAR,
                node.getLineNumber()
        );
        if (node.getValue() instanceof Ast.Python.PythonExprNode) {
            Ast.Python.PythonExprNode valueExpr = (Ast.Python.PythonExprNode) node.getValue();
            switch (valueExpr.getKind()) {
                case LIST:
                    entry.setDataType("list");
                    break;
                case STRING:
                    entry.setDataType("str");
                    break;
                case BOOL:
                    entry.setDataType("bool");
                    break;
                case NONE:
                    entry.setDataType("NoneType");
                    break;
                default:
                    break;
            }
        }

        currentScope.define(node.getTarget(), entry);
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
    }

    @Override
    public void visit(PythonImportNode node) {
        if (node.getKind() == PythonImportNode.ImportKind.IMPORT_MODULE) {
            String moduleName = node.getSource();
            SymbolEntry entry = new SymbolEntry(
                    moduleName,
                    SymbolEntry.SymbolType.PYTHON_IMPORT,
                    node.getLineNumber()
            );
            currentScope.define(moduleName, entry);

        } else {
            for (String name : node.getNames()) {
                SymbolEntry entry = new SymbolEntry(
                        name,
                        SymbolEntry.SymbolType.PYTHON_IMPORT,
                        node.getLineNumber()
                );
                currentScope.define(name, entry);
            }
        }
    }

    @Override
    public void visit(PythonCallNode node) {
    }

    @Override
    public void visit(PythonExprNode node) {
        if (node.getKind() == PythonExprNode.ExprKind.BINARY) {
            if (node.getLeft() != null) node.getLeft().accept(this);
            if (node.getRight() != null) node.getRight().accept(this);
        }
    }

    @Override
    public void visit(PythonIfNode node) {
        if (node.getCondition() != null) node.getCondition().accept(this);
        if (node.getThenBlock() != null) node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) node.getElseBlock().accept(this);
    }

    @Override
    public void visit(PythonForNode node) {
        SymbolEntry entry = new SymbolEntry(
                node.getLoopVar(),
                SymbolEntry.SymbolType.PYTHON_VAR,
                node.getLineNumber()
        );
        currentScope.define(node.getLoopVar(), entry);

        if (node.getBody() != null) node.getBody().accept(this);
    }

    @Override
    public void visit(PythonReturnNode node) {
        if (node.getValue() != null) node.getValue().accept(this);
    }

    @Override
    public void visit(PythonDecoratorNode node) {}
    @Override
    public void visit(PythonCommentNode node) {}

    // Jinja Nodes
    @Override
    public void visit(JinjaForNode node) {
        enterScope("For_Loop_" + node.getLoopVar());

        SymbolEntry loopVarEntry = new SymbolEntry(
                node.getLoopVar(),
                SymbolEntry.SymbolType.JINJA_LOOP_VAR,
                node.getLineNumber()
        );
        currentScope.define(node.getLoopVar(), loopVarEntry);

        if (node.getBody() != null) node.getBody().accept(this);

        exitScope();
    }

    @Override
    public void visit(JinjaIfNode node) {
        if (node.getThenBlock() != null) node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) node.getElseBlock().accept(this);
    }

    @Override
    public void visit(JinjaExprNode node) {
    }

    @Override
    public void visit(JinjaBlockNode node) {
        if (node.getStatements() != null) {
            for (ASTNode stmt : node.getStatements()) stmt.accept(this);
        }
    }

    // HTML Nodes
    @Override
    public void visit(HtmlElementNode node) {
        if (node.getContent() != null) {
            for (ASTNode child : node.getContent().getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(HtmlOpenNode node) {}
    @Override
    public void visit(HtmlCloseNode node) {}
    @Override
    public void visit(HtmlSelfCloseNode node) {}
    @Override
    public void visit(HtmlTextNode node) {}
    @Override
    public void visit(HtmlContentNode node) {
        for (ASTNode child : node.getChildren()) child.accept(this);
    }

    @Override
    public void visit(HtmlBlockNode node) {
        if (node.getOpening() != null) node.getOpening().accept(this);
        if (node.getContent() != null) node.getContent().accept(this);
        if (node.getClosing() != null) node.getClosing().accept(this);
    }

    @Override
    public void visit(AttributeNode node) {}
    @Override
    public void visit(DoctypeNode node) {}
    @Override
    public void visit(WordNode node) {}

    // CSS Nodes
    @Override
    public void visit(CssBlockNode node) {
        for (ASTNode child : node.getChildren()) child.accept(this);
    }

    @Override
    public void visit(DeclarationNode node) {}
    @Override
    public void visit(SelectorListNode node) {
        for (ASTNode selector : node.getSelectors()) selector.accept(this);
    }

    @Override
    public void visit(SelectorNode node) {}
    @Override
    public void visit(SelectorRuleNode node) {}
    @Override
    public void visit(ClassSelectorNode node) {}
    @Override
    public void visit(ValueNode node) {}

    // Common
    @Override
    public void visit(BlockNode node) {
        for (ASTNode child : node.getChildren()) child.accept(this);
    }

    public ScopedSymbolTable getGlobalScope() {
        return globalScope;
    }
}