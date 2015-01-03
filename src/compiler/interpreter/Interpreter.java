package compiler.interpreter;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 目标代码的生成和解释执行
 * Created by zhugongpu on 14/12/3.
 */
public class Interpreter {

    private static final int StackSize = 1000;

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

    /**
     * 模拟栈式计算机。
     * 拥有一个栈式数据段用于存放运行时数据, 拥有一个代码段用于存放PCODE程序代码。
     * 同时还拥用数据段分配指针、指令指针、指令寄存器、局部段基址指针等寄存器。
     */
    public void interpret(BufferedReader reader, BufferedWriter writer) throws IOException {

        int[] runtimeStack = new int[StackSize];//程序运行栈
        Arrays.fill(runtimeStack, 0);//初始化

        //Start Interpret PCode
        int programCounter = 0;//程序计数器
        int basePointer = 0;//指令基指针
        int stackPointer = 0;//栈顶指针

        do {

            PCode pCode = pcodes.get(programCounter);
            programCounter++;

            //TODO not implemented yet

            switch (pCode.getCodeType()) {
                case LIT://将值取到栈顶
                    runtimeStack[stackPointer++] = pCode.getArgument();
                    break;
                case OPR://数学、逻辑运算
                    switch (pCode.getArgument()) {
                        //TODO


                        case 0:// OPR 0 0: RETURN
                            stackPointer = basePointer;
                            programCounter = runtimeStack[stackPointer + 2];
                            basePointer = runtimeStack[stackPointer + 1];
                            break;
                        case 1://OPR 0 1:取反
                            runtimeStack[stackPointer - 1] = -runtimeStack[stackPointer - 1];
                            break;
                        case 2://OPR 0 2:ADD
                            stackPointer--;
                            runtimeStack[stackPointer - 1] += runtimeStack[stackPointer];
                            break;
                        case 3://OPR 0 3:SUB
                            stackPointer--;
                            runtimeStack[stackPointer - 1] -= runtimeStack[stackPointer];
                            break;
                        case 4://OPR 0 4:MUL
                            stackPointer--;
                            runtimeStack[stackPointer - 1] *= runtimeStack[stackPointer];
                            break;
                        case 5://OPR 0 5:DIV
                            stackPointer--;
                            runtimeStack[stackPointer - 1] /= runtimeStack[stackPointer];
                            break;
                        case 6://OPR 0 6:MOD 2
                            //TODO 是否需要sp--;
                            runtimeStack[stackPointer - 1] %= 2;
                            break;
                        case 7://OPR 0 7:MOD
                            stackPointer--;
                            runtimeStack[stackPointer - 1] %= runtimeStack[stackPointer];
                            break;
                        case 8://OPR 0 8:EQUAL
                            stackPointer--;
                            runtimeStack[stackPointer - 1] = (runtimeStack[stackPointer] == runtimeStack[stackPointer - 1] ? 1 : 0);
                            break;
                        case 9://OPR 0 9:NOT EQUAL
                            stackPointer--;
                            runtimeStack[stackPointer - 1] = (runtimeStack[stackPointer] != runtimeStack[stackPointer - 1] ? 1 : 0);
                            break;
                        case 10://OPR 0 10:LESS THAN
                            stackPointer--;
                            runtimeStack[stackPointer - 1] = (runtimeStack[stackPointer] < runtimeStack[stackPointer - 1] ? 1 : 0);
                            break;
                        case 11://OPR 0 11:GREATER THAN OR EQUAL
                            stackPointer--;
                            runtimeStack[stackPointer - 1] = (runtimeStack[stackPointer] >= runtimeStack[stackPointer - 1] ? 1 : 0);
                            break;
                        case 12://OPR 0 12:GREATER THAN
                            stackPointer--;
                            runtimeStack[stackPointer - 1] = (runtimeStack[stackPointer] > runtimeStack[stackPointer - 1] ? 1 : 0);
                            break;
                        case 13://OPR 0 13:LESS THAN OR EQUAL
                            stackPointer--;
                            runtimeStack[stackPointer - 1] = (runtimeStack[stackPointer] <= runtimeStack[stackPointer - 1] ? 1 : 0);
                            break;
                        case 14://OPR 0 14:输出栈顶值
                            System.out.println("runtimeStack[stackPointer - 1] = " + runtimeStack[stackPointer - 1]);

                            writer.write(" " + runtimeStack[stackPointer - 1] + ' ');
                            writer.flush();

                            stackPointer--;
                            break;
                        case 15://OPR 0 15:输出换行
                            System.out.print("\n");

                            writer.write('\n');//todo 采用newLine
                            writer.flush();
                            break;
                        case 16://OPR 0 16:读入一行数据，置入栈顶
                            System.out.println("请输入一个整数:");
                            runtimeStack[stackPointer] = 0;

                            runtimeStack[stackPointer] = Integer.parseInt(reader.readLine().trim());
                            System.out.println(runtimeStack[stackPointer]);
                            stackPointer++;
//TODO 可能逻辑错误，应该为sp-1
                            writer.write(" " + runtimeStack[stackPointer] + ' ');
                            writer.flush();

                            break;
                    }
                    break;
                case LOD://取相对当前过程的数据基地址为a的内存的值到栈顶
                    runtimeStack[stackPointer++] = runtimeStack[getBaseStackPointer(pCode.getLevelDifference(), runtimeStack, basePointer)];
                    break;
                case STO://栈顶的值存到相对当前的过程的数据基地址为a的内存
                    runtimeStack[getBaseStackPointer(pCode.getLevelDifference(), runtimeStack, basePointer) + pCode.getArgument()]
                            = runtimeStack[--stackPointer];
                    break;
                case CAL://调用子程序
                    runtimeStack[stackPointer] = getBaseStackPointer(pCode.getLevelDifference(), runtimeStack, basePointer);//将静态作用域基地址入栈
                    runtimeStack[stackPointer + 1] = basePointer;//将动态作用域基地址入栈
                    runtimeStack[stackPointer + 2] = programCounter;//将当前指针入栈
                    basePointer = stackPointer;//改变基地址指针值为新过程的基地址
                    programCounter = pCode.getArgument();//跳转至地址argument
                    break;
                case INT://开辟空间
                    stackPointer += pCode.getArgument();
                    break;
                case JMP://跳转
                    programCounter = pCode.getArgument();
                    break;
                case JPC:
                    if (runtimeStack[--stackPointer] == 0) {//当栈顶指针为0时，条件跳转
                        programCounter = pCode.getArgument();
                    }
                    break;
                default:
                    break;
            }
        } while (programCounter != 0);
    }

    /**
     * 通过给定的层次差来获得该层的堆栈帧基址
     *
     * @param levelDifference  目标层次与当前层粗的层次差
     * @param runtimeStack     运行时栈
     * @param baseStackPointer 当前层堆栈基地址
     * @return 目标层次的堆栈基地址
     */
    private int getBaseStackPointer(int levelDifference, int[] runtimeStack, int baseStackPointer) {
        while (levelDifference > 0) {
            baseStackPointer = runtimeStack[baseStackPointer];
            levelDifference--;
        }

        return baseStackPointer;
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
