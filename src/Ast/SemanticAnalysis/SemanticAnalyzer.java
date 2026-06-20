package Ast.SemanticAnalysis;

import Ast.*;
import Ast.Css.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Python.*;
import SymbolTable.ScopedSymbolTable;
import SymbolTable.SymbolEntry;
import Visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer implements ASTVisitor {

    private final ScopedSymbolTable globalScope;
    private ScopedSymbolTable currentScope;
    private final List<SemanticError> errors = new ArrayList<>();

    public SemanticAnalyzer(ScopedSymbolTable globalScope) {
        this.globalScope = globalScope;
        this.currentScope = globalScope;
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    public int getErrorCount() {
        return errors.size();
    }

    private void enterScope(String childName) {
        ScopedSymbolTable child = currentScope.getChildScope(childName);
        if (child != null) {
            currentScope = child;
        }
    }

    private void exitScope() {
        if (currentScope.getParent() != null) {
            currentScope = currentScope.getParent();
        }
    }

    // Program
    @Override
    public void visit(ProgramNode node) {
        for (ASTNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
    }

    // Python
    @Override
    public void visit(PythonFunctionNode node) {
        enterScope("Function_" + node.getName());

        if (node.getBody() != null)
            node.getBody().accept(this);

        exitScope();
    }

    @Override
    public void visit(PythonAssignNode node) {
        if (node.getValue() != null)
            node.getValue().accept(this);
    }

    @Override
    public void visit(PythonImportNode node) {
    }

    @Override
    public void visit(PythonCallNode node) {
        String callee = node.getCallee();
        if (callee == null) return;

        String searchKey = callee.contains(".") ? callee.split("\\.")[0] : callee;

        SymbolEntry entry = currentScope.lookup(searchKey);
        if (entry == null) {
            String[] builtIns = {
                    "len", "print", "range", "str", "int", "float",
                    "list", "dict", "open", "type", "isinstance",
                    "chain_call", "dict_entry", "complex_call"
            };
            boolean isBuiltin = false;
            for (String b : builtIns) {
                if (searchKey.equals(b)) {
                    isBuiltin = true;
                    break;
                }
            }
            if (!isBuiltin) {
                errors.add(new UndefinedFunctionError(searchKey, node.getLineNumber()));
            }
            return;
        }

        if (entry.getType() == SymbolEntry.SymbolType.PYTHON_FUNCTION
                && entry.getExpectedParamCount() >= 0) {

            int expected = entry.getExpectedParamCount();
            int actual = (node.getArgs() != null) ? node.getArgs().size() : 0;

            if (actual != expected) {
                errors.add(new WrongArgumentCountError(
                        searchKey, expected, actual, node.getLineNumber()
                ));
            }
        }
    }

    @Override
    public void visit(PythonExprNode node) {
        if (node.getKind() == PythonExprNode.ExprKind.IDENTIFIER) {
            String name = node.getRawText();
            if (name != null) {
                SymbolEntry entry = currentScope.lookup(name);
                if (entry == null) {
                    String[] builtInVars = {
                            "request", "session", "g", "current_app", "app",
                            "True", "False", "None", "__name__", "__file__",
                            "self", "cls", "args", "kwargs"
                    };
                    boolean isBuiltin = false;
                    for (String bv : builtInVars) {
                        if (name.equals(bv)) {
                            isBuiltin = true;
                            break;
                        }
                    }
                    if (!isBuiltin) {
                        errors.add(new UndefinedVariableError(name, node.getLineNumber()));
                    }
                }
            }
        }
        else if (node.getKind() == PythonExprNode.ExprKind.BINARY) {
            checkBinaryTypeMismatch(node);

            if (node.getLeft() != null) node.getLeft().accept(this);
            if (node.getRight() != null) node.getRight().accept(this);
        }
    }

    private void checkBinaryTypeMismatch(PythonExprNode node) {
        String leftListVar = getListTypedIdentifier(node.getLeft());
        String rightListVar = getListTypedIdentifier(node.getRight());
        boolean leftIsString = isStringLiteral(node.getLeft());
        boolean rightIsString = isStringLiteral(node.getRight());

        if (leftListVar != null && rightIsString) {
            errors.add(new InvalidTypeError(
                    "cannot compare list '" + leftListVar + "' with a string",
                    node.getLineNumber()
            ));
        } else if (rightListVar != null && leftIsString) {
            errors.add(new InvalidTypeError(
                    "cannot compare list '" + rightListVar + "' with a string",
                    node.getLineNumber()
            ));
        }
    }

    private String getListTypedIdentifier(ASTNode side) {
        if (side instanceof PythonExprNode) {
            PythonExprNode expr = (PythonExprNode) side;
            if (expr.getKind() == PythonExprNode.ExprKind.IDENTIFIER) {
                SymbolEntry entry = currentScope.lookup(expr.getRawText());
                if (entry != null && "list".equals(entry.getDataType())) {
                    return expr.getRawText();
                }
            }
        }
        return null;
    }

    private boolean isStringLiteral(ASTNode side) {
        return side instanceof PythonExprNode
                && ((PythonExprNode) side).getKind() == PythonExprNode.ExprKind.STRING;
    }

    @Override
    public void visit(PythonIfNode node) {
        if (node.getCondition() != null) node.getCondition().accept(this);
        if (node.getThenBlock() != null) node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) node.getElseBlock().accept(this);
    }

    @Override
    public void visit(PythonForNode node) {
        if (node.getBody() != null)
            node.getBody().accept(this);
    }

    @Override
    public void visit(PythonReturnNode node) {
        if (node.getValue() != null)
            node.getValue().accept(this);
    }

    @Override
    public void visit(PythonDecoratorNode node) {}
    @Override
    public void visit(PythonCommentNode node) {}

    // Jinja
    @Override
    public void visit(JinjaForNode node) {
        enterScope("For_Loop_" + node.getLoopVar());

        if (node.getBody() != null)
            node.getBody().accept(this);

        exitScope();
    }

    @Override
    public void visit(JinjaIfNode node) {
        if (node.getThenBlock() != null) node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) node.getElseBlock().accept(this);
    }

    @Override
    public void visit(JinjaExprNode node) {
        String varName = node.getVariableName();
        if (varName != null) {
            String base = varName.contains(".") ? varName.split("\\.")[0] : varName;
            SymbolEntry entry = currentScope.lookup(base);
            if (entry == null) {
                errors.add(new UndefinedJinjaVariableError(base, node.getLineNumber()));
            }
        }
    }

    @Override
    public void visit(JinjaBlockNode node) {
        if (node.getStatements() != null) {
            for (ASTNode stmt : node.getStatements())
                stmt.accept(this);
        }
    }

    // HTML
    @Override
    public void visit(HtmlElementNode node) {
        if (node.getContent() != null) {
            for (ASTNode child : node.getContent().getChildren())
                child.accept(this);
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
        for (ASTNode child : node.getChildren())
            child.accept(this);
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

    // CSS
    @Override
    public void visit(CssBlockNode node) {
        for (ASTNode child : node.getChildren())
            child.accept(this);
    }

    @Override
    public void visit(SelectorRuleNode node) {}
    @Override
    public void visit(SelectorListNode node) {
        for (ASTNode s : node.getSelectors())
            s.accept(this);
    }

    @Override
    public void visit(SelectorNode node) {}
    @Override
    public void visit(ClassSelectorNode node) {}
    @Override
    public void visit(DeclarationNode node) {}
    @Override
    public void visit(ValueNode node) {}

    // Block
    @Override
    public void visit(BlockNode node) {
        boolean returnSeen = false;
        for (ASTNode child : node.getChildren()) {
            if (returnSeen) {
                errors.add(new UnreachableCodeError(child.getLineNumber()));
            }
            if (child instanceof PythonReturnNode) {
                returnSeen = true;
            }
            child.accept(this);
        }
    }
}