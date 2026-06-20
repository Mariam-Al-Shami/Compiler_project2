package Visitor;

import Ast.*;
import Ast.Css.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Python.*;

public interface ASTVisitor {
    void visit(ClassSelectorNode node);
    void visit(CssBlockNode node);
    void visit(DeclarationNode node);
    void visit(SelectorListNode node);
    void visit(SelectorNode node);
    void visit(SelectorRuleNode node);
    void visit(ValueNode node);
    void visit(AttributeNode node);
    void visit(DoctypeNode node);
    void visit(HtmlBlockNode node);
    void visit(HtmlCloseNode node);
    void visit(HtmlContentNode node);
    void visit(HtmlElementNode node);
    void visit(HtmlOpenNode node);
    void visit(HtmlSelfCloseNode node);
    void visit(HtmlTextNode node);
    void visit(WordNode node);
    void visit(JinjaBlockNode node);
    void visit(JinjaForNode node);
    void visit(JinjaExprNode node);
    void visit(JinjaIfNode node);
    void visit(PythonFunctionNode node);
    void visit(PythonAssignNode node);
    void visit(PythonCallNode node);
    void visit(PythonCommentNode node);
    void visit(PythonDecoratorNode node);
    void visit(PythonExprNode node);
    void visit(PythonImportNode node);
    void visit(PythonIfNode node);
    void visit(PythonForNode node);
    void visit(PythonReturnNode node);
    void visit(BlockNode node);
    void visit(ProgramNode node);
}