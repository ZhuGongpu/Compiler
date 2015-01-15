package compiler.error;

import java.io.PrintStream;

/**
 * 错误处理
 * Created by zhugongpu on 14/12/1.
 */
public class ErrorHandler {
    /**
     * 错误信息
     */
    public static final String[] errorInfo = new String[]{
            "",
            " 1.应是=而不是:=",
            " 2.=后应为数",
            " 3.标识符后应为=",
            " 4.const,var,procedure 后应为标识符",
            " 5.漏掉逗号或分号",
            " 6.过程说明后的符号不正确",
            " 7.应为语句",
            " 8.程序体内语句后的符号不正确",
            " 9.应为句号",
            "10.语句之间漏分号",
            "11.标识符未说明",
            "12.不可向常量或过程名赋值",
            "13.应为赋值运算符:=",
            "14.call后应为标识符",
            "15.不可调用常量或变量",
            "16.应为then",
            "17.应为分号或end",
            "18.应为do",
            "19.语句后的符号不正确",
            "20.应为关系运算符",
            "21.表达式内不可有过程标识符",
            "22.漏右括号",
            "23.因子后不可为此符号",
            "24.表达式不能以此符号开始",
            "25.缺少until语句",
            "26.应为左括号",
            "27.Not Defined Yet",
            "28.Not Defined Yet",
            "29.Not Defined Yet",
            "30.这个数太大",
            "31.数越界",
            "32.嵌套层数过大",
            "33.read语句中应该为变量",
            "34.格式错误，应为左括号",
            "35.read()中的变量未声明",
            "36.Not Defined Yet",
            "37.Not Defined Yet",
            "38.Not Defined Yet",
            "39.Not Defined Yet",
            "40.应为左括号"


    };
    /**
     * 总的错误数量
     */
    private int totalErrorCount = 0;
    /**
     * 用户输出错误信息
     */
    private PrintStream errorPrinter = null;

    public ErrorHandler(PrintStream errorPrinter) {
        this.errorPrinter = errorPrinter;
    }

    /**
     * 输出错误信息
     *
     * @param errorCode 错误代码
     * @param location  出错的位置
     */
    public void printError(int errorCode, String location) {
        totalErrorCount++;
        String errorMessage = "ERROR: " + errorInfo[errorCode] + " at line#" + location;
        printErrorInfo(errorMessage);
    }

    /**
     * @return 返回总的错误个数
     */
    public int getTotalErrorCount() {
        return totalErrorCount;
    }

    private void printErrorInfo(String message) {
        errorPrinter.println(message);
        errorPrinter.flush();
    }

}
