package sample.compiler;

/**
 * 采用全局变量sym来存储符号码，并用全局变量id和num来传递语义值
 *
 * @author jiangnan
 */
public class Symbol {

    //各类符号码
    public static final int nul = 0;                  //NULL
    public static final int ident = 1;               //标识符
    public static final int plus = 2;                //加号+
    public static final int minus = 3;              //减号-
    public static final int mul = 4;                 //乘号*
    public static final int div = 5;                  //除号/
    public static final int oddsym = 6;           //odd
    public static final int number = 7;           //数字
    public static final int eql = 8;                  //等于号=(equal)
    public static final int neq = 9;                 //不等于<>(not equal)
    public static final int lss = 10;                 //小于<(less)
    public static final int geq = 11;                 //大于等于>=(greater or equal)
    public static final int gtr = 12;                //大于>(greater)
    public static final int leq = 13;                //小于等于<=(less or equal)
    public static final int lparen = 14;            //左括号(
    public static final int rparen = 15;           //右括号 ) 
    public static final int comma = 16;           //逗号,
    public static final int semicolon = 17;       //分号;
    public static final int peroid = 18;            //句号.
    public static final int becomes = 19;         //赋值符号 :=
    public static final int beginsym = 20;        //开始符号begin
    public static final int endsym = 21;           //结束符号end
    public static final int ifsym = 22;             //if
    public static final int thensym = 23;         //then
    public static final int whilesym = 24;        //while
    public static final int writesym = 25;        //write
    public static final int readsym = 26;         //read
    public static final int dosym = 27;            //do
    public static final int callsym = 28;          //call
    public static final int constsym = 29;       //const
    public static final int varsym = 30;           //var
    public static final int procsym = 31;         //procedure
    public static final int elsesym = 32;
    public static final int repeatsym = 33;
    public static final int untilsym = 34;

    //符号码的个数
    public static final int symnum = 35;

    //设置保留字名字，按照字母顺序，便于折半查找
    public static final String[] word = new String[]{
            "begin", "call", "const", "do",
            "else", "end", "if", "odd",
            "procedure", "read", "repeat", "then",
            "until", "var", "while", "write"};
    //保留字对应的符号值
    public static final int[] wsym = new int[]{
            beginsym, callsym, constsym, dosym,
            elsesym, endsym, ifsym, oddsym,
            procsym, readsym, repeatsym, thensym,
            untilsym, varsym, whilesym, writesym};

    //符号码
    public int symtype;
    //标志符号名字；
    public String id;
    //数值的大小
    public int num;

    /**
     * 构造具有特定符号码的符号
     *
     * @param stype
     */
    public Symbol(int stype) {
        symtype = stype;
        id = "";
        num = 0;
    }
}
