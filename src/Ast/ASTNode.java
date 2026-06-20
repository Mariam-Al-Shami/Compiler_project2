package Ast;

import Visitor.ASTVisitor;

public abstract class ASTNode {
    protected String nodeName;
    protected int lineNumber;
    protected int columnNumber;

    public ASTNode(String nodeName, int lineNumber, int columnNumber) {
        this.nodeName = nodeName;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() { return lineNumber; }
    public int getColumnNumber() { return columnNumber; }
    public String getNodeName() { return nodeName; }
    public abstract String print(String indent);
    public abstract void accept(ASTVisitor visitor);
}