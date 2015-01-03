package compiler.symbol_table;

/**
 * 符号表中的一条记录
 * Created by zhugongpu on 14/12/3.
 */
public class Tuple {
    /**
     * 名字
     */
    public String name = null;
    /**
     * 种类
     */
    public TupleType kind = TupleType.VARIABLE;
    /**
     * 常量的值
     * 仅当kind为CONSTANT时使用
     */
    public int value = -1;
    /**
     * 分程序所处层次
     * 主程序的层次为0
     */
    public int level;
    /**
     * 每层局部变量所分配单元的相应地址(其起始值为3，由地址分配索引变量指定)
     * 仅当kind为VARIABLE或PROCEDURE时使用。
     * 对于PROCEDURE，应填入编译该过程所生成的P-Code指令序列的入口地址
     */
    public int address;

    /**
     * 需要分配的数据区空间，仅procedure使用
     */
    public int size;

    public enum TupleType {
        CONSTANT, VARIABLE, PROCEDURE
    }
}
