package compiler.lexical;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
     */
    private char currentChar = ' ';

    /**
     * 用于标记getChar时是否需要读入字符
     * 当在retract中设置为false，再次调用getChar时相当于后退一个字符
     */
    private boolean needRead = true;

    /**
     * 处理文件输入
     */
    private BufferedReader bufferedReader = null;

    public Scanner(String filePath) throws FileNotFoundException {
        bufferedReader = new BufferedReader(new FileReader(filePath));
    }

    private static boolean isSpace(char currentChar) {
        return currentChar == ' ';
    }

    private static boolean isNewLine(char currentChar) {
        return currentChar == '\n';
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

    private static boolean isSemi(char currentChar) {
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

    private static boolean isLeftParenthesis(char currentChar) {
        return currentChar == '(';
    }

    private static boolean isRightParenthesis(char currentChar) {
        return currentChar == ')';
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
                return new Symbol(Symbol.SymbolClassCode.ASSIGN, ":=");
            else {//PL0文法中没有单独':'的情况，因此这种情况下算作出错
                retract();
                error();
                return null;
            }

        } else if (isPlus(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.PLUS, "+");
        } else if (isMinus(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.MINUS, "-");
        } else if (isStar(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.MULTIPLY, "*");
        } else if (isLeftParenthesis(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.LEFT_PARENTHESIS, "(");
        } else if (isRightParenthesis(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.RIGHT_PARENTHESIS, ")");
        } else if (isComma(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.COMMA, ",");
        } else if (isSemi(currentChar)) {
            return new Symbol(Symbol.SymbolClassCode.SEMICOLON, ";");
        } else if (isDivide(currentChar)) {//由于PL0文法中没有注释，因此不需要考虑这种情况
            return new Symbol(Symbol.SymbolClassCode.DIVIDE, "/");
        } else {
            retract();
            error();
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

        retract();//后退一个字符

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

        retract();//后退一个字符

        int number = Integer.parseInt(buffer.toString());

        return new Symbol(Symbol.SymbolClassCode.NUMBER, number);
    }


    /**
     * 读入下一个字符
     *
     * @return 当读到流末尾时，返回-1
     */
    private int getChar() throws IOException {
        if (needRead)
            currentChar = (char) bufferedReader.read();
        return currentChar;
    }

    /**
     * 将读字符指针后退一个字符
     */
    private void retract() {
        needRead = false;
    }

    /**
     * 出错
     */
    private void error() {

    }
}
