package compiler.interpreter;


import java.util.ArrayList;

/**
 * 目标代码的生成和解释执行
 * Created by zhugongpu on 14/12/3.
 */
public class Interpreter {


    /**
     * 保存PCode
     */
    private ArrayList<PCode> pcodes = new ArrayList<PCode>();

    /**
     * 获取pcodes中的指定元素
     *
     * @param index
     * @return
     */
    public PCode getPCodeAtIndex(int index) {
        return pcodes.get(index);
    }

    /**
     * 设置pcode数组中的指定元素
     *
     * @param index
     * @param pcode
     */
    public void setPCodeAtIndex(int index, PCode pcode) {
        if (index >= 0 && index < pcodes.size()) {
            pcodes.set(index, pcode);
        }
    }

    /**
     * 获取当前指令索引指针的位置，用来表示下一条要生成的指令的地址
     * 即cx
     *
     * @return
     */
    public int getCodeIndex() {
        return pcodes.size();
    }

    /**
     * 生成代码
     *
     * @param type            操作码
     * @param levelDifference 变量或过程被引用的分程序与说明该变量或过程的分程序之间的层次差
     * @param argument        参数
     */
    public void genPCode(PCode.CodeType type, int levelDifference, int argument) {
        pcodes.add(new PCode(type, levelDifference, argument));
    }


    public void interprete() {
        //TODO 
    }

    /**
     * 打印所有PCodes
     */
    public void printPCodes() {
        printPCodes(0);
    }

    /**
     * 打印PCodes
     *
     * @param startIndex 起始下标
     */
    public void printPCodes(int startIndex) {
        int length = pcodes.size();

        System.out.println("--------------- PCodes ---------------");
        for (int i = startIndex; i < length; i++) {
            pcodes.get(i).print();
        }
        System.out.println("---------------        ---------------");

    }
}
