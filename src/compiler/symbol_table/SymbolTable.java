package compiler.symbol_table;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * 符号表
 * Created by zhugongpu on 14/12/2.
 */
public class SymbolTable {

    /**
     * 允许的最大嵌套层数
     */
    public static final int MAX_LEVEL = 3;
    public static int MAX_NUMBER = Integer.MAX_VALUE;//支持的整数的最大值
    /**
     * 符号表
     */
    private ArrayList<Tuple> table = new ArrayList<Tuple>();
    /**
     * 当前符号表项指针（有效的符号表大小）
     */
    private int tableIndex = 0;//TODO 用途不明

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    /**
     * 向符号表中插入一条记录
     *
     * @param tuple
     */
    private void enter(Tuple tuple) {
        tableIndex++;
        this.table.add(tuple);//TODO 需要改为在tableIndex处添加tuple
    }

    /**
     * 将常量登录到符号表中
     *
     * @param identifier 常量的标识符
     * @param value      常量的值
     */
    public void enterConstant(String identifier, int value) {
        Tuple tuple = new Tuple();
        tuple.kind = Tuple.TupleType.CONSTANT;
        tuple.name = identifier;
        tuple.value = value;
        enter(tuple);
    }

    /**
     * 将变量登录到符号表中
     *
     * @param identifier 变量名
     * @param level      所处层级
     * @param address    偏移地址，调用之后需要将dx+1
     */
    public void enterVariable(String identifier, int level, int address) {
        Tuple tuple = new Tuple();
        tuple.kind = Tuple.TupleType.VARIABLE;
        tuple.name = identifier;
        tuple.level = level;
        tuple.address = address;
        enter(tuple);
    }

    /**
     * 将过程登录到符号表中
     *
     * @param identifier 过程名
     * @param level      所处层级
     */
    public void enterProcedure(String identifier, int level) {
        Tuple tuple = new Tuple();
        tuple.kind = Tuple.TupleType.PROCEDURE;
        tuple.name = identifier;
        tuple.level = level;
    }

    /**
     * 查找标识符在符号表中的位置
     * 在对各种语句进行分析处理时，凡遇到标识符，都要调用该方法去查找符号表
     *
     * @param identifier
     * @return 若该标识符在table中已定义，则返回它在table中的位置；若table中不包含该标识符，则返回-1
     */
    public int position(String identifier) {

        for (int i = table.size() - 1; i >= 0; i--) {
            if (table.get(i).name.equals(identifier)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 返回符号表中的一条记录
     *
     * @param index
     * @return
     */
    public Tuple getTupleAtIndex(int index) {
        if (index >= table.size())
            table.add(index, new Tuple());

        return table.get(index);
    }

    /**
     * 输出符号表中所有内容
     */
    public void printTable() {
        printTable(0);
    }

    public void printTable(int startIndex) {
        printTable(startIndex, System.out);
    }

    public void printTable(PrintStream outputStream) {
        printTable(0, outputStream);
    }

    /**
     * 输出符号表中内容
     *
     * @param startIndex 起始位置
     */
    private void printTable(int startIndex, PrintStream outputStream) {
//TODO 可能需要根据tableIndex进行修改
        int tableSize = table.size();

        System.out.printf("table size = %d\n", tableSize);

        System.out.println("------------------- Symbol Table -------------------");

        outputStream.printf("%10s%10s%10s%10s%10s\n", "name", "kind", "value", "level", "address");

        if (startIndex > tableSize)
            System.out.println("<NULL>");

        for (int i = startIndex; i < tableSize; i++) {

            String name = "<NULL>";
            String type = "<NULL>";
            String value = "<NULL>";
            String level = "<NULL>";
            String address = "<NULL>";

            Tuple tuple = table.get(i);
            name = tuple.name;
            switch (tuple.kind) {
                case CONSTANT:
                    type = "Const";
                    value = tuple.value + "";
                    break;
                case VARIABLE:
                    type = "Var";
                    level = tuple.level + "";
                    address = tuple.address + "";
                    break;
                case PROCEDURE:
                    type = "Pro";
                    level = tuple.level + "";
                    address = tuple.address + "";
                    break;
            }

            outputStream.printf("%10s%10s%10s%10s%10s\n", name, type, value, level, address);
        }

        System.out.println("----------------------------------------------------");
    }
}
