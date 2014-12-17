package compiler.lexical;

/**
 * Created by zhugongpu on 14/12/1.
 */
public class Symbol {
    /**
     * 保留字
     * 按照字母顺序，便于折半查找
     */
    public static final String[] ReservedWords = new String[]{
            "begin",
            "call",
            "const",
            "do",
            "else",
            "end",
            "if",
            "odd",
            "procedure",
            "read",
            "repeat",
            "then",
            "until",
            "var",
            "while",
            "write"};

    /**
     * ReservedWords对应的符号类别编码
     */
    public static final SymbolClassCode[] ReservedWordCodes = new SymbolClassCode[]{
            SymbolClassCode.BEGIN,
            SymbolClassCode.CALL,
            SymbolClassCode.CONST,
            SymbolClassCode.DO,
            SymbolClassCode.ELSE,
            SymbolClassCode.END,
            SymbolClassCode.IF,
            SymbolClassCode.ODD,
            SymbolClassCode.PROCEDURE,
            SymbolClassCode.READ,
            SymbolClassCode.REPEAT,
            SymbolClassCode.THEN,
            SymbolClassCode.UNTIL,
            SymbolClassCode.VAR,
            SymbolClassCode.WHILE,
            SymbolClassCode.WRITE
    };
    /**
     * 若为标识符，保存表字符名称
     */
    private String token = null;
    /**
     * 若为整型，保存整数值
     */
    private int value;
    /**
     * Symbol的类型编码
     */
    private SymbolClassCode symbolClassCode;

    /**
     * 用于表示标识符
     *
     * @param symbolClassCode
     * @param token
     */
    public Symbol(SymbolClassCode symbolClassCode, String token) {
        this.symbolClassCode = symbolClassCode;
        this.token = token;
    }

    /**
     * 用于表示整型
     * 整形变量/常量的标识符可以通过set方法设置
     *
     * @param symbolClassCode
     * @param value
     */
    public Symbol(SymbolClassCode symbolClassCode, int value) {
        this.symbolClassCode = symbolClassCode;
        this.value = value;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public SymbolClassCode getSymbolClassCode() {
        return symbolClassCode;
    }

    /**
     * 词法分析程序中的单词类别编码
     * 按照在PL0文法中出现的顺序排列
     * Created by zhugongpu on 14/12/1.
     */
    public static enum SymbolClassCode {
        NULL,
        IDENTIFIER,//标识符
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        ODD,
        NUMBER,//数字
        EQUAL,//=
        NOT_EQUAL,//<>
        LESS_THAN,//<
        GREATER_THAN_OR_EQUAL,//>=
        GREATER_THAN,//>
        LESS_THAN_OR_EQUAL,//<=
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        COMMA,
        SEMICOLON,//;
        PERIOD,//.
        ASSIGN,//:=
        BEGIN,
        END,
        IF,
        THEN,
        WHILE,
        WRITE,
        READ,
        DO,
        CALL,
        CONST,
        VAR,
        PROCEDURE,
        ELSE,
        REPEAT,
        UNTIL,

    }
}
