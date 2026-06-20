package SymbolTable;

public class SymbolEntry {
    private final String name;
    private final SymbolType type;
    private final int lineNumber;
    private String dataType;
    private int expectedParamCount = -1;

    public enum SymbolType {
        PYTHON_FUNCTION,
        PYTHON_VAR,
        PYTHON_PARAM,
        PYTHON_IMPORT,
        JINJA_LOOP_VAR,
    }

    public SymbolEntry(String name, SymbolType type, int lineNumber) {
        this.name = name;
        this.type = type;
        this.lineNumber = lineNumber;
        this.dataType = "unknown";
    }

    public SymbolEntry(String name, SymbolType type, int lineNumber, String dataType) {
        this.name = name;
        this.type = type;
        this.lineNumber = lineNumber;
        this.dataType = (dataType != null) ? dataType : "unknown";
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getExpectedParamCount() {
        return expectedParamCount;
    }

    public void setExpectedParamCount(int expectedParamCount) {
        this.expectedParamCount = expectedParamCount;
    }

    @Override
    public String toString() {
        String base = "Name: " + name + " | Type: " + type + " | Line: " + lineNumber;
        if (!"unknown".equals(dataType)) {
            base += " | DataType: " + dataType;
        }
        if (type == SymbolType.PYTHON_FUNCTION && expectedParamCount >= 0) {
            base += " | Params: " + expectedParamCount;
        }
        return base;
    }
}