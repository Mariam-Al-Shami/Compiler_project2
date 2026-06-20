package Visitor;


import Ast.*;
import Ast.Css.*;
import Ast.Html.*;
import Ast.Jinja.*;
import Ast.Python.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import antlr.*;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

public class ASTBuilderVisitor extends exampleParserBaseVisitor<ASTNode> {

    //PROGRAM
    @Override
    public ASTNode visitProgram(exampleParser.ProgramContext ctx) {
        ProgramNode program = new ProgramNode(ctx.getStart().getLine());
        ctx.children.stream()
                .filter(c -> !(c instanceof TerminalNode))
                .map(this::visit)
                .forEach(node -> { if(node != null) program.addStatement(node); });
        return program;
    }

    // PYTHON — pythonBlock labels
    @Override
    public ASTNode visitPythonStatement(exampleParser.PythonStatementContext ctx) {
        return visit(ctx.statement());
    }

    @Override
    public ASTNode visitPythonImport(exampleParser.PythonImportContext ctx) {
        return visit(ctx.importSTMT());
    }

    @Override
    public ASTNode visitPythonComment(exampleParser.PythonCommentContext ctx) {
        String text = ctx.getStart().getText();
        return new PythonCommentNode(text, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitPythonAssign(exampleParser.PythonAssignContext ctx) {
        return visit(ctx.assignmentStmt());
    }

    @Override
    public ASTNode visitPythonExpr(exampleParser.PythonExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitPythonList(exampleParser.PythonListContext ctx) {
        return visit(ctx.listExpr());
    }

    @Override
    public ASTNode visitPythonFuncDef(exampleParser.PythonFuncDefContext ctx) {
        return visit(ctx.functionDef());
    }

    @Override
    public ASTNode visitPythonReturn(exampleParser.PythonReturnContext ctx) {
        return visit(ctx.returnSTMT());
    }

    @Override
    public ASTNode visitPythonDecorator(exampleParser.PythonDecoratorContext ctx) {
        return visit(ctx.functionAt());
    }

    @Override
    public ASTNode visitPythonIf(exampleParser.PythonIfContext ctx) {
        return visit(ctx.ifStmt());
    }

    @Override
    public ASTNode visitPythonNewline(exampleParser.PythonNewlineContext ctx) {
        return null;
    }


    // PYTHON — statement labels
    @Override
    public ASTNode visitStmtImport(exampleParser.StmtImportContext ctx) {
        return visit(ctx.importSTMT());
    }

    @Override
    public ASTNode visitStmtAssign(exampleParser.StmtAssignContext ctx) {
        return visit(ctx.assignmentStmt());
    }

    @Override
    public ASTNode visitStmtExpr(exampleParser.StmtExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitStmtList(exampleParser.StmtListContext ctx) {
        return visit(ctx.listExpr());
    }

    @Override
    public ASTNode visitStmtReturn(exampleParser.StmtReturnContext ctx) {
        return visit(ctx.returnSTMT());
    }

    @Override
    public ASTNode visitStmtDecorator(exampleParser.StmtDecoratorContext ctx) {
        return visit(ctx.functionAt());
    }

    //  PYTHON — import rules

    @Override
    public ASTNode visitImportFromModule(exampleParser.ImportFromModuleContext ctx) {
        String source = ctx.ID(0).getText();
        List<String> names = new ArrayList<>();
        for (int i = 1; i < ctx.ID().size(); i++) {
            names.add(ctx.ID(i).getText());
        }
        return new PythonImportNode(PythonImportNode.ImportKind.FROM_MODULE,
                source, names, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitImportModule(exampleParser.ImportModuleContext ctx) {
        List<String> ids = ctx.ID().stream()
                .map(id -> id.getText())
                .collect(Collectors.toList());
        String source = ids.isEmpty() ? "" : ids.get(0);
        List<String> names = ids.size() > 1 ? ids.subList(1, ids.size()) : new ArrayList<>();
        return new PythonImportNode(PythonImportNode.ImportKind.IMPORT_MODULE,
                source, names, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitImportPath(exampleParser.ImportPathContext ctx) {
        String source = ctx.modulePath().getText();
        List<String> names = new ArrayList<>();
        if (ctx.ID() != null) names.add(ctx.ID().getText());
        return new PythonImportNode(PythonImportNode.ImportKind.FROM_PATH,
                source, names, ctx.getStart().getLine());
    }

    //  PYTHON — assignment rules

    @Override
    public ASTNode visitAssignVar(exampleParser.AssignVarContext ctx) {
        String  target = ctx.ID().getText();
        ASTNode value  = visit(ctx.expr());
        return new PythonAssignNode(PythonAssignNode.AssignKind.VAR,
                target, value, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitAssignIndexed(exampleParser.AssignIndexedContext ctx) {
        String  target = ctx.indexedAccess().getText();
        ASTNode value  = visit(ctx.expr());
        return new PythonAssignNode(PythonAssignNode.AssignKind.INDEXED,
                target, value, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitAssignCall(exampleParser.AssignCallContext ctx) {
        String target = ctx.modulePath().getText();
        return new PythonAssignNode(PythonAssignNode.AssignKind.CALL,
                target, null, ctx.getStart().getLine());
    }

    //  PYTHON — function definition

    @Override
    public ASTNode visitFuncDef(exampleParser.FuncDefContext ctx) {
        String name = ctx.ID().getText();
        List<String> params = new ArrayList<>();
        if (ctx.paramList() != null) {
            String rawParams = ctx.paramList().getText();
            String[] parts = rawParams.split(",");
            for (String part : parts) {
                String cleanName = part;
                int eqIndex = cleanName.indexOf('=');
                if (eqIndex != -1) {
                    cleanName = cleanName.substring(0, eqIndex);
                }
                int colonIndex = cleanName.indexOf(':');
                if (colonIndex != -1) {
                    cleanName = cleanName.substring(0, colonIndex);
                }

                cleanName = cleanName.trim();
                if (!cleanName.isEmpty()) {
                    params.add(cleanName);
                }
            }
        }

        BlockNode body = (ctx.block() != null)
                ? (BlockNode) visit(ctx.block())
                : new BlockNode(ctx.getStart().getLine());
        return new PythonFunctionNode(name, params, body, ctx.getStart().getLine());
    }

    //  PYTHON — decorator

    @Override
    public ASTNode visitDecoratorCall(exampleParser.DecoratorCallContext ctx) {
        String path = ctx.modulePath().getText();
        List<String> args = ctx.contentAt().stream()
                .map(c -> c.getText())
                .filter(t -> !t.equals(","))
                .collect(Collectors.toList());
        return new PythonDecoratorNode(path, args, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitDecoratorString(exampleParser.DecoratorStringContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.STRING,
                ctx.STRING().getText(), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitDecoratorArgList(exampleParser.DecoratorArgListContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.LIST,
                ctx.getText(), ctx.getStart().getLine());
    }

    //  PYTHON — if statement

    @Override
    public ASTNode visitIfStatement(exampleParser.IfStatementContext ctx) {
        ASTNode   condition = visit(ctx.expr());
        BlockNode thenBlock = (BlockNode) visit(ctx.thenBlock);
        BlockNode elseBlock = (ctx.elseBlock != null)
                ? (BlockNode) visit(ctx.elseBlock)
                : null;
        return new PythonIfNode(condition, thenBlock, elseBlock,
                ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitPythonFor(exampleParser.PythonForContext ctx) {
        return visit(ctx.forStmt());
    }

    @Override
    public ASTNode visitStmtFor(exampleParser.StmtForContext ctx) {
        return visit(ctx.forStmt());
    }

    @Override
    public ASTNode visitForStatement(exampleParser.ForStatementContext ctx) {
        String loopVar = ctx.ID().getText();
        ASTNode iterable = visit(ctx.expr());
        BlockNode body = (BlockNode) visit(ctx.block());
        return new PythonForNode(loopVar, iterable, body, ctx.getStart().getLine());
    }
    //  PYTHON — block

    @Override
    public ASTNode visitBlockIndented(exampleParser.BlockIndentedContext ctx) {
        BlockNode block = new BlockNode(ctx.getStart().getLine());
        if (ctx.children != null) {
            ctx.children.stream()
                    .filter(c -> !(c instanceof TerminalNode))
                    .map(c -> {
                        try { return visit(c); }
                        catch (Exception e) { return null; }
                    })
                    .filter(n -> n != null)
                    .forEach(block::addStatement);
        }
        return block;
    }

    //  PYTHON — return statements

    @Override
    public ASTNode visitReturnModuleFunc(exampleParser.ReturnModuleFuncContext ctx) {
        String rawText = ctx.getText().replaceFirst("^return", "").trim();
        ASTNode value = (ctx.returnValue() != null)
                ? visit(ctx.returnValue())
                : new PythonExprNode(PythonExprNode.ExprKind.STRING,
                ctx.STRING() != null ? ctx.STRING().getText() : "",
                ctx.getStart().getLine());
        return new PythonReturnNode(value, rawText, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitReturnStringNumber(exampleParser.ReturnStringNumberContext ctx) {
        String rawText = ctx.STRING().getText() + ", " + ctx.INT().getText();
        ASTNode value  = new PythonExprNode(PythonExprNode.ExprKind.STRING,
                rawText, ctx.getStart().getLine());
        return new PythonReturnNode(value, rawText, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitReturnVarAttr(exampleParser.ReturnVarAttrContext ctx) {
        String rawText = ctx.getText().replaceFirst("^return", "").trim();
        return new PythonExprNode(PythonExprNode.ExprKind.IDENTIFIER,
                rawText, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitReturnStringAssign(exampleParser.ReturnStringAssignContext ctx) {
        String rawText = ctx.getText().replaceFirst("^return", "").trim();
        return new PythonExprNode(PythonExprNode.ExprKind.STRING,
                rawText, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitReturnFuncCall(exampleParser.ReturnFuncCallContext ctx) {
        List<String> args = new ArrayList<>();
        args.add(ctx.STRING().getText());
        return new PythonCallNode(ctx.ID().getText(), args, ctx.getStart().getLine());
    }

    //  PYTHON — expressions

    @Override
    public ASTNode visitBinaryExpr(exampleParser.BinaryExprContext ctx) {
        ASTNode left  = visit(ctx.left);
        ASTNode right = visit(ctx.right);
        String  op    = ctx.simpleExpr().getText();
        return new PythonExprNode(left, op, right, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallExprExpr(exampleParser.CallExprExprContext ctx) {
        return visit(ctx.callExpr());
    }

    @Override
    public ASTNode visitListExprExpr(exampleParser.ListExprExprContext ctx) {
        return visit(ctx.listExpr());
    }

    @Override
    public ASTNode visitBuiltInExpr(exampleParser.BuiltInExprContext ctx) {
        return visit(ctx.variableBuiltIn());
    }

    @Override
    public ASTNode visitStringExpr(exampleParser.StringExprContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.STRING,
                ctx.STRING().getText(), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitModuleExpr(exampleParser.ModuleExprContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.IDENTIFIER,
                ctx.ID().getText(), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitBoolExpr(exampleParser.BoolExprContext ctx) {
        String val = ctx.TRUE() != null ? "True" : "False";
        return new PythonExprNode(PythonExprNode.ExprKind.BOOL,
                val, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitNoneExpr(exampleParser.NoneExprContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.NONE,
                "None", ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitVarBuiltIn(exampleParser.VarBuiltInContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.BUILTIN,
                ctx.getText(), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitListLiteral(exampleParser.ListLiteralContext ctx) {
        List<String> items = ctx.expr().stream()
                .map(e -> e.getText())
                .collect(Collectors.toList());
        return new PythonExprNode(PythonExprNode.ExprKind.LIST,
                items.toString(), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitModulePathRule(exampleParser.ModulePathRuleContext ctx) {
        return new PythonExprNode(PythonExprNode.ExprKind.IDENTIFIER,
                ctx.getText(), ctx.getStart().getLine());
    }

    //  PYTHON — call expressions

    @Override
    public ASTNode visitCallSimple(exampleParser.CallSimpleContext ctx) {
        String callee = ctx.ID(0).getText();
        List<String> args = ctx.expr().stream()
                .map(e -> e.getText())
                .collect(Collectors.toList());
        return new PythonCallNode(callee, args, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallCompare(exampleParser.CallCompareContext ctx) {
        String path = ctx.modulePath().getText();
        boolean isDunder = path.startsWith("__") && path.endsWith("__");
        boolean isSimpleName = !path.contains(".");

        if (isDunder || isSimpleName) {
            ASTNode left = isDunder
                    ? new PythonExprNode(PythonExprNode.ExprKind.BUILTIN, path, ctx.getStart().getLine())
                    : new PythonExprNode(PythonExprNode.ExprKind.IDENTIFIER, path, ctx.getStart().getLine());
            ASTNode right = new PythonExprNode(
                    PythonExprNode.ExprKind.STRING, ctx.STRING().getText(), ctx.getStart().getLine());
            String operator = ctx.simpleExpr().getText();
            return new PythonExprNode(left, operator, right, ctx.getStart().getLine());
        }

        return new PythonCallNode(ctx.modulePath().getText(),
                List.of(ctx.STRING().getText()), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallPath(exampleParser.CallPathContext ctx) {
        List<String> args = ctx.expr().stream()
                .map(e -> e.getText())
                .collect(Collectors.toList());
        return new PythonCallNode(ctx.modulePath().getText(),
                args, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallModuleFunc(exampleParser.CallModuleFuncContext ctx) {
        return new PythonCallNode(ctx.modulePath().getText(),
                List.of(ctx.getText()), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallMethod(exampleParser.CallMethodContext ctx) {
        return new PythonCallNode(ctx.modulePath().getText(),
                List.of(ctx.getText()), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallFuncOneArg(exampleParser.CallFuncOneArgContext ctx) {
        return new PythonCallNode(ctx.ID(0).getText(),
                List.of(ctx.ID(1).getText()), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallComplex(exampleParser.CallComplexContext ctx) {
        return new PythonCallNode("complex_call",
                List.of(ctx.getText()), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallChain(exampleParser.CallChainContext ctx) {
        return new PythonCallNode("chain_call",
                List.of(ctx.getText()), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallWithBlock(exampleParser.CallWithBlockContext ctx) {
        BlockNode block = (BlockNode) visit(ctx.block());
        return new PythonCallNode(ctx.modulePath().getText(),
                List.of(), ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCallMap(exampleParser.CallMapContext ctx) {
        return new PythonCallNode("dict_entry",
                List.of(ctx.STRING().getText(), ctx.ID().getText()),
                ctx.getStart().getLine());
    }


    // HTML VISITOR METHODS

    @Override
    public ASTNode visitHtmlNormal(exampleParser.HtmlNormalContext ctx) {
        HtmlElementNode element = (HtmlElementNode) visit(ctx.htmlOpen());
        if (ctx.htmlContent() != null) {
            StringBuilder textAccumulator = new StringBuilder();

            for (var child : ctx.htmlContent().children) {
                ASTNode node = visit(child);
                if (node != null) {
                    addAccumulatedText(element, textAccumulator, ctx.getStart().getLine());
                    element.addChild(node);
                }
                else if (child instanceof TerminalNode tn) {
                    int tokenType = tn.getSymbol().getType();
                    String rawText = tn.getText();

                    if (tokenType == exampleParser.INDENT || tokenType == exampleParser.DEDENT || rawText.trim().isEmpty()) {
                        continue;
                    }

                    textAccumulator.append(rawText).append(" ");
                }
            }
            addAccumulatedText(element, textAccumulator, ctx.getStart().getLine());
        }
        return element;
    }

    private void addAccumulatedText(HtmlElementNode element, StringBuilder accumulator, int line) {
        if (accumulator.length() > 0) {
            String finalContent = accumulator.toString().trim();
            if (!finalContent.isEmpty()) {
                element.addChild(new HtmlTextNode(finalContent, line));
            }
            accumulator.setLength(0);
        }
    }

    @Override
    public ASTNode visitHtmlOpenRule(exampleParser.HtmlOpenRuleContext ctx) {
        HtmlElementNode element = new HtmlElementNode(ctx.HTML_TAG_NAME().getText(), ctx.getStart().getLine());
        ctx.attribute().forEach(attr -> element.addAttribute((AttributeNode) visit(attr)));
        return element;
    }

    @Override
    public ASTNode visitHtmlAttribute(exampleParser.HtmlAttributeContext ctx) {
        String name = ctx.ATTRIBUTE_NAME().getText();
        String value = (ctx.ATTRIBUTE_VALUE() != null)
                ? ctx.ATTRIBUTE_VALUE().getText().replaceAll("^['\"]|['\"]$", "")
                : null;
        return new AttributeNode(name, value, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitHtmlSelfCloseTag(exampleParser.HtmlSelfCloseTagContext ctx) {
        HtmlSelfCloseNode element = new HtmlSelfCloseNode(ctx.HTML_TAG_NAME_SELF_CLOSE().getText(), ctx.getStart().getLine());
        ctx.attribute().forEach(attr -> element.addAttribute((AttributeNode) visit(attr)));
        return element;
    }

    @Override
    public ASTNode visitHtmlDoctypeDecl(exampleParser.HtmlDoctypeDeclContext ctx) {
        return new DoctypeNode(ctx.HTML_TAG_NAME().getText(), ctx.getStart().getLine());
    }

    public ASTNode visitJinjaForBlock(exampleParser.JinjaForBlockContext ctx) {
        return (ASTNode)this.visit(ctx.jinjaForStmt());
    }

    public ASTNode visitJinjaIfBlock(exampleParser.JinjaIfBlockContext ctx) {
        return (ASTNode)this.visit(ctx.jinjaIfStmt());
    }

    public ASTNode visitJinjaExprBlock(exampleParser.JinjaExprBlockContext ctx) {
        return (ASTNode)this.visit(ctx.jinjaExpr());
    }

    public ASTNode visitJinjaForStmtRule(exampleParser.JinjaForStmtRuleContext ctx) {
        String loopVar = ctx.JINJA_NAME(0).getText();
        String iterable = ctx.JINJA_NAME(1).getText();
        BlockNode body = this.buildBlockFromHtmlContent(ctx.htmlContent());
        return new JinjaForNode(loopVar, iterable, body, ctx.getStart().getLine());
    }

    public ASTNode visitJinjaIfStmtRule(exampleParser.JinjaIfStmtRuleContext ctx) {
        String condition = ctx.JINJA_NAME().getText();
        BlockNode thenBlock = this.buildBlockFromHtmlContent(ctx.htmlContent());
        BlockNode elseBlock = null;
        return new JinjaIfNode(condition, thenBlock, (BlockNode)elseBlock, ctx.getStart().getLine());
    }

    public ASTNode visitJinjaExprRule(exampleParser.JinjaExprRuleContext ctx) {
        String variableName = ctx.JINJA_NAME().getText();
        return new JinjaExprNode(variableName, ctx.getStart().getLine());
    }

    private BlockNode buildBlockFromHtmlContent(exampleParser.HtmlContentContext htmlContent) {
        BlockNode block = new BlockNode(htmlContent != null ? htmlContent.getStart().getLine() : 0);
        if (htmlContent == null) {
            return block;
        } else {
            Iterator var3 = htmlContent.children.iterator();

            while(var3.hasNext()) {
                ParseTree child = (ParseTree)var3.next();
                if (!(child instanceof TerminalNode)) {
                    ASTNode node = (ASTNode)this.visit(child);
                    if (node != null) {
                        block.addStatement(node);
                    }
                }
            }

            return block;
        }
    }



    //CSS VISITOR METHODS

    @Override
    public ASTNode visitCssBlockRule(exampleParser.CssBlockRuleContext ctx) {
        CssBlockNode block = new CssBlockNode(ctx.getStart().getLine());
        ctx.selectorRule().forEach(rule -> block.addRule(visit(rule)));
        return block;
    }

    @Override
    public ASTNode visitCssSelectorList(exampleParser.CssSelectorListContext ctx) {
        List<DeclarationNode> decls = ctx.declaration().stream()
                .map(d -> (DeclarationNode) visit(d)).collect(Collectors.toList());

        return new SelectorRuleNode((SelectorListNode) visit(ctx.selectorList()), decls, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCssClassSelector(exampleParser.CssClassSelectorContext ctx) {
        String className = ctx.CLASS_SELECTOR().getText();
        SelectorNode pseudo = (ctx.cssSelector() != null) ? (SelectorNode) visit(ctx.cssSelector()) : null;

        List<DeclarationNode> decls = ctx.declaration().stream()
                .map(d -> (DeclarationNode) visit(d)).collect(Collectors.toList());

        return new ClassSelectorNode(className, pseudo, decls, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCssClassHover(exampleParser.CssClassHoverContext ctx) {
        String className = ctx.CLASS_SELECTOR().getText();
        String pseudoName = ctx.PSEUDO_CLASS().getText();
        SelectorNode pseudo = new SelectorNode(pseudoName, null, ctx.getStart().getLine());

        List<DeclarationNode> decls = ctx.declaration().stream()
                .map(d -> (DeclarationNode) visit(d))
                .collect(Collectors.toList());

        return new ClassSelectorNode(className, pseudo, decls, ctx.getStart().getLine());
    }
    @Override
    public ASTNode visitCssSelectorListItems(exampleParser.CssSelectorListItemsContext ctx) {
        List<SelectorNode> selectors = ctx.cssSelector().stream()
                .map(s -> (SelectorNode) visit(s)).collect(Collectors.toList());
        return new SelectorListNode(selectors, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCssSelectorRule(exampleParser.CssSelectorRuleContext ctx) {
        String name = ctx.SELECTOR().getText();
        String pseudo = (ctx.PSEUDO_CLASS() != null) ? ctx.PSEUDO_CLASS().getText() : null;
        return new SelectorNode(name, pseudo, ctx.getStart().getLine());
    }

    @Override
    public ASTNode visitCssDeclaration(exampleParser.CssDeclarationContext ctx) {
        String property = ctx.PROPERTY_NAME().getText();

        List<ValueNode> values = ctx.children.stream()
                .filter(c -> c instanceof TerminalNode)
                .map(c -> (TerminalNode) c)
                .filter(tn -> tn.getSymbol().getType() == exampleParser.PROPERTY_VALUE ||
                        tn.getSymbol().getType() == exampleParser.COMMA_CSS)
                .map(tn -> new ValueNode(tn.getText(), tn.getSymbol().getLine()))
                .collect(Collectors.toList());
        return new DeclarationNode(property, values, ctx.getStart().getLine());
    }

}