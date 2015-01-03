package compiler.interpreter;

/**
 * PCode指令代码
 * Created by zhugongpu on 14/12/3.
 */
public class PCode {
    /**
     * 各指令对应的字符串表示
     */
    private static final String[] code = new String[]{
            "LIT",
            "OPR",
            "LOD",
            "STO",
            "CAL",
            "INT",
            "JMP",
            "JPC"};
    /**
     * 操作码
     */
    private CodeType codeType;
    /**
     * 变量或过程被引用的分程序与说明该变量或过程的分程序之间的层次差
     */
    private int levelDifference;
    /**
     * 参数
     */
    private int argument;

    /**
     * @param codeType        操作码
     * @param levelDifference 变量或过程被引用的分程序与说明该变量或过程的分程序之间的层次差
     * @param argument        参数
     */
    public PCode(CodeType codeType, int levelDifference, int argument) {
        this.codeType = codeType;
        this.levelDifference = levelDifference;
        this.argument = argument;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public int getLevelDifference() {
        return levelDifference;
    }

    public int getArgument() {
        return argument;
    }

    /**
     * 设置argument
     *
     * @param argument
     */
    public void setArgument(int argument) {
        this.argument = argument;
    }

    /**
     * 打印PCode
     */
    public void print() {
//        System.out.printf("%s %d, %d", code[codeType.ordinal()], levelDifference, argument);
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        return "PCode{" +
                "codeType=" + codeType +
                ", levelDifference=" + levelDifference +
                ", argument=" + argument +
                '}';
    }

    public enum CodeType {
        LIT,
        OPR,
        LOD,
        STO,
        CAL,
        INT,
        JMP,
        JPC
    }
}
