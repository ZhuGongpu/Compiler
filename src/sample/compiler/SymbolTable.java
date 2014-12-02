package sample.compiler;
//竟然没有对符号表检查大小，会溢出的。

import java.io.IOException;

public class SymbolTable {

    /**
     * 符号表的大小
     */
    public static final int tableMax = 100;
    public static final int symMax = 10;            //符号的最大长度
    public static final int addrMax = 1000000;        //最大允许的数值
    public static final int levMax = 3;            //最大允许过程嵌套声明层数[0,levmax]
    public static final int numMax = 14;           //number的最大位数
    public static boolean tableswitch;           //显示名字表与否
    /**
     * 当前名字表项指针(有效的符号表大小)table size
     */
    public int tablePtr = 0;
    //名字表
    public Item[] table = new Item[tableMax];

    /**
     * 获得名字表某一项的内容
     *
     * @param i 名字表中的位置
     * @return 名字表第i项的内容
     */
    public Item get(int i) {
        if (table[i] == null) {
            table[i] = new Item();
        }
        return table[i];
    }

    /**
     * 把某个符号登录到名字表中 名字表从1开始填，0表示不存在该项符号
     *
     * @param sym 要登记到名字表的符号
     * @param k   该符号的类型：const,var,procedure
     * @param lev 名字所在的层次
     * @param dx  当前应分配的变量的相对地址，注意调用enter()后dx要加一
     */
    public void enter(Symbol sym, int type, int lev, int dx) {
        tablePtr++;
        Item item = get(tablePtr);
        item.name = sym.id;
        item.type = type;
        switch (type) {
            case Item.constant:                                     //常量名字
                item.value = sym.num;                               //记录下常数值的大小
                break;
            case Item.variable:                                      //变量名字
                item.lev = lev;                                          //变量所在的层
                item.addr = dx;                                            //变量的偏移地址
                break;
            case Item.procedure:                                    //过程名字
                item.lev = lev;

        }
    }

    /**
     * 在名字表中查找某个名字的位置 查找符号表是从后往前查， 这样符合嵌套分程序名字定义和作用域的规定
     *
     * @param idt 要查找的名字
     * @return 如果找到则返回名字项的下标，否则返回0
     */
    public int position(String idt) {
        for (int i = tablePtr; i > 0; i--) //必须从后往前找
        {
            if (get(i).name.equals(idt)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 输出符号表内容，摘自block()函数
     *
     * @param start 当前符号表区间的左端
     */
    void debugTable(int start) {
        if (tableswitch) //显示名字表与否
        {
            return;
        }
        System.out.println("**** Symbol Table ****");
        if (start > tablePtr) {
            System.out.println("  NULL");
        }
        for (int i = start + 1; i <= tablePtr; i++) {
            try {
                String msg = "unknown table item !";
                switch (table[i].type) {
                    case Item.constant:
                        msg = "   " + i + "  const: " + table[i].name + "  val: " + table[i].value;
                        break;
                    case Item.variable:
                        msg = "    " + i + "  var: " + table[i].name + "  lev: " + table[i].lev + "  addr: " + table[i].addr;
                        break;
                    case Item.procedure:
                        msg = "    " + i + " proc: " + table[i].name + "  lev: " + table[i].lev + "  addr: " + table[i].size;
                        break;
                }
                System.out.println(msg);
                PL0.tableWriter.write(msg + '\n');
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("***write table intfo meet with error***");
            }
        }
    }

    public class Item {

        public static final int constant = 0;
        public static final int variable = 1;
        public static final int procedure = 2;
        String name;                                             //名字
        int type;                                               //类型，const var or procedur
        int value;                                                 //数值，const使用
        int lev;                                                 //所处层，var和procedur使用
        int addr;                                                //地址，var和procedur使用
        int size;                                               //需要分配的数据区空间，仅procedure使用

        public Item() {
            super();
            this.name = "";
        }

    }
}
