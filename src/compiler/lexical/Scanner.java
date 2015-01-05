package compiler.lexical;

import compiler.error.ErrorHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * 词法分析器
 * 作为一个独立的子过程由语法分析器调用
 * <p/>
 * 主要功能：
 * 1. 跳过源程序中的空格字符
 * 2. 从源程序正文字符序列中识别出单词符号，并把该单词符号的类别以相应枚举值的形式（即内部编码）送入变量sym中
 * 3. 用变量id存放标识符，用二分法查找保留字表，识别诸如begin、end等保留字
 * 4. 如取来的单词为无符号整数，则将该整数数字字符串转换为整数值存入变量num中
 * <p/>
 * Created by zhugongpu on 14/12/1.
 */
public class Scanner {

    /**
     * 存放当前读进的字符
     * 初始化为' '，使得首次调用getSymbol时可以执行getChar
     */
    private char currentChar = ' ';

    /**
     * 当前扫描的行号
     */
    private int currentLineNumber = 1;

    /**
     * 处理文件输入
     */
    private BufferedReader bufferedReader = null;
    /**
     * 处理错误信息
     */
    private ErrorHandler errorHandler = null;

    public Scanner(BufferedReader bufferedReader, ErrorHandler errorHandler) throws FileNotFoundException {
        this.bufferedReader = bufferedReader;
        this.errorHandler = errorHandler;
    }

    private static boolean isSpace(char currentChar) {
        return currentChar == ' ';
    }

    private static boolean isNewLine(char currentChar) {
        return currentChar == '\n' || currentChar == '\r';
    }

    private static boolean isTab(char currentChar) {
        return currentChar == '\t';
    }

    private static boolean isLetter(char currentChar) {
        return (currentChar >= 'a' && currentChar <= 'z') || currentChar >= 'A' && currentChar <= 'Z';
    }

    private static boolean isDigit(char currentChar) {
        return (currentChar >= '0' && currentChar <= '9');
    }

    private static boolean isColon(char currentChar) {
        return currentChar == ':';
    }

    private static boolean isComma(char currentChar) {
        return currentChar == ',';
    }

    private static boolean isSemicolon(char currentChar) {
        return currentChar == ';';
    }

    private static boolean isEqual(char currentChar) {
        return currentChar == '=';
    }

    private static boolean isPlus(char currentChar) {
        return currentChar == '+';
    }

    private static boolean isMinus(char currentChar) {
        return currentChar == '-';
    }

    private static boolean isDivide(char currentChar) {
        return currentChar == '/';
    }

    private static boolean isStar(char currentChar) {
        return currentChar == '*';
    }

    private static boolean isLessThan(char currentChar) {
        return currentChar == '<';
    }

    private static boolean isGreaterThan(char currentChar) {
        return currentChar == '>';
    }

    private static boolean isLeftParenthesis(char currentChar) {
        return currentChar == '(';
    }

    private static boolean isRightParenthesis(char currentChar) {
        return currentChar == ')';
    }

    private static boolean isPeriod(char currentChar) {
        return currentChar == '.';
    }

    /**
     * 词法分析器
     */
    public Symbol getSymbol() throws IOException {

        while (isSpace(currentChar) || isNewLine(currentChar) || isTab(currentChar)) getChar();//读取字符，跳过空格

        if (isLetter(currentChar)) {//判断当前字符是否为一个字母
            return getKeyWordOrIdentifier();
        } else if (isDigit(currentChar)) {//判断当前字符是否为数字
            return getNumber();
        } else if (isColon(currentChar)) {

            getChar();
            if (isEqual(currentChar))//为 赋值符号
            {
                getChar();
                return new Symbol(Symbol.SymbolClassCode.ASSIGN, ":=");
            } else {//PL0文法中没有单独':'的情况，因此这种情况下算作出错
                error(26);//TODO 错误未定义
                return null;
            }

        } else if (isEqual(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.EQUAL, "=");
        } else if (isLessThan(currentChar)) {//为 小于号或小于等于号
            getChar();
            if (isEqual(currentChar)) {
                getChar();
                return new Symbol(Symbol.SymbolClassCode.LESS_THAN_OR_EQUAL, "<=");
            }
            return new Symbol(Symbol.SymbolClassCode.LESS_THAN, "<");
        } else if (isGreaterThan(currentChar)) {
            getChar();
            if (isEqual(currentChar)) {
                getChar();
                return new Symbol(Symbol.SymbolClassCode.GREATER_THAN_OR_EQUAL, ">=");
            }
            return new Symbol(Symbol.SymbolClassCode.GREATER_THAN, ">");
        } else if (isPlus(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.PLUS, "+");
        } else if (isMinus(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.MINUS, "-");
        } else if (isStar(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.MULTIPLY, "*");
        } else if (isLeftParenthesis(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.LEFT_PARENTHESIS, "(");
        } else if (isRightParenthesis(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.RIGHT_PARENTHESIS, ")");
        } else if (isComma(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.COMMA, ",");
        } else if (isSemicolon(currentChar)) {
            getChar();
            return new Symbol(Symbol.SymbolClassCode.SEMICOLON, ";");
        } else if (isDivide(currentChar)) {//由于PL0文法中没有注释，因此不需要考虑这种情况
            getChar();
            return new Symbol(Symbol.SymbolClassCode.DIVIDE, "/");
        } else if (isPeriod(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.PERIOD, ".");
        } else {

            error(26);//TODO 错误未定义
        }
        return null;
    }

    /**
     * 获取关键字或标识符
     *
     * @return
     * @throws IOException
     */
    private Symbol getKeyWordOrIdentifier() throws IOException {
        StringBuffer buffer = new StringBuffer();
        do {
            buffer.append(currentChar);
            getChar();
        } while (isLetter(currentChar) || isDigit(currentChar));

        String token = buffer.toString();
        int indexInReservedWords = Arrays.binarySearch(Symbol.ReservedWords, token);//二分查找，检查当前token是否为保留字
        Symbol.SymbolClassCode classCode;
        if (indexInReservedWords < 0) {//标识符
            classCode = Symbol.SymbolClassCode.IDENTIFIER;
        } else {//保留字
            classCode = Symbol.ReservedWordCodes[indexInReservedWords];
        }

        return new Symbol(classCode, token);
    }

    /**
     * 获取数字
     *
     * @return
     * @throws IOException
     */
    private Symbol getNumber() throws IOException {
        StringBuffer buffer = new StringBuffer();

        do {
            buffer.append(currentChar);
            getChar();
        } while (isDigit(currentChar));

        int number = Integer.parseInt(buffer.toString());

        return new Symbol(Symbol.SymbolClassCode.NUMBER, number);
    }

    /**
     * 读入下一个字符
     *
     * @return 当读到流末尾时，返回-1
     */
    private int getChar() throws IOException {
        //记录行号
        if (currentChar == '\n')
            currentLineNumber++;

        currentChar = (char) bufferedReader.read();
        printDebugInfo("get char " + currentChar + "(" + (int) currentChar + ")" + " at line#" + currentLineNumber);

        return currentChar;
    }

    /**
     * 输出错误信息
     */
    private void error(int errorCode) {
        errorHandler.printError(errorCode, getCurrentLineNumber());
    }

    private void printDebugInfo(String message) {
        System.out.println(message);
        System.out.flush();
    }

    /**
     * 返回当前行号
     *
     * @return
     */
    public int getCurrentLineNumber() {
        return currentLineNumber;
    }
}
