package SymbolTable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScopedSymbolTable {
    private final String scopeName;
    private final int scopeLevel;
    private final ScopedSymbolTable parent;
    private final Map<String, SymbolEntry> symbols;
    private final Map<String, ScopedSymbolTable> children;

    public ScopedSymbolTable(String scopeName, int scopeLevel, ScopedSymbolTable parent) {
        this.scopeName = scopeName;
        this.scopeLevel = scopeLevel;
        this.parent = parent;
        this.symbols = new LinkedHashMap<>();
        this.children = new LinkedHashMap<>();
    }

    public void define(String name, SymbolEntry symbol) {
        symbols.put(name, symbol);
    }

    public SymbolEntry lookup(String name) {
        var symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }

    public SymbolEntry lookupCurrentScopeOnly(String name) {
        return symbols.get(name);
    }

    public ScopedSymbolTable createChildScope(String childName) {
        ScopedSymbolTable child = new ScopedSymbolTable(childName, this.scopeLevel + 1, this);
        children.put(childName, child);
        return child;
    }

    public ScopedSymbolTable getChildScope(String childName) {
        return children.get(childName);
    }

    public Map<String, ScopedSymbolTable> getChildren() {
        return children;
    }

    public ScopedSymbolTable getParent() {
        return parent;
    }

    public String getScopeName() {
        return scopeName;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public void printTable() {
        System.out.println("--- Scope: " + scopeName + " (Level " + scopeLevel + ") ---");
        for (Map.Entry<String, SymbolEntry> entry : symbols.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public void printTableRecursive() {
        printTable();
        for (ScopedSymbolTable child : children.values()) {
            child.printTableRecursive();
        }
    }
}