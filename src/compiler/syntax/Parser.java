package compiler.syntax;

import compiler.error.ErrorHandler;
import compiler.interpreter.Interpreter;
import compiler.interpreter.PCode;
import compiler.lexical.Scanner;
import compiler.lexical.Symbol;
import compiler.symbol_table.SymbolTable;
import compiler.symbol_table.Tuple;

import java.io.IOException;
import java.util.BitSet;

/**
 * 语法分析器
 * <p/>
 * 采用递归子程序法进行语法分析，即为每一个语法成分都编写了一个分析子程序，根据当前读取的符号，可以选择相应的子程序进行语法分析。
 * Created by zhugongpu on 14/12/2.
 */
public class Parser {
    /**
     * 词法分析器
     */
    private Scanner lexicalScanner = null;
    /**
     * 符号表
     */
    private SymbolTable symbolTable = null;

    /**
     * 代码生成程序
     */
    private Interpreter interpreter = null;

    /**
     * 出错处理
     */
    private ErrorHandler errorHandler = null;

    /**
     * 读入的当前符号
     */
    private Symbol currentSymbol = null;


    /**
     * <声明>的First集合
     */
    private BitSet firstSetOfDeclaration = null;
    /**
     * <语句>的First集合
     */
    private BitSet firstSetOfStatement = null;
    /**
     * <因子>的First集
     */
    private BitSet firstSetOfFactor = null;

    /**
     * 当前作用域的堆栈帧大小，或者说数据大小(data size)
     * 计算每个变量在运行栈中相对本过程基地址的偏移量，
     * 放在symbolTable中的address域，
     * 生成目标代码时再放在code中的a域
     */
    private int dataAllocationIndex = 0;

    /**
     * 语法分析程序
     *
     * @param lexicalScanner 词法分析程序
     * @param symbolTable    符号表
     * @param interpreter    代码解释生成程序
     */
    public Parser(Scanner lexicalScanner, SymbolTable symbolTable, Interpreter interpreter) {
        this.lexicalScanner = lexicalScanner;
        this.symbolTable = symbolTable;
        this.interpreter = interpreter;
        this.errorHandler = new ErrorHandler();


        /**
         * 设置申明开始符号集
         * <分程序> ::= [<常量说明部分>][<变量说明部分>]{<过程说明部分>}<语句>
         * <常量说明部分> ::= const<常量定义>{,<常量定义>};
         * <变量说明部分>::= var<标识符>{,<标识符>};
         * <过程说明部分> ::= <过程首部>procedure<标识符>; <分程序>;
         * FIRST(declaration)={const var procedure null};
         */
        firstSetOfDeclaration = new BitSet(Symbol.SymbolClassCode.values().length);
        firstSetOfDeclaration.set(Symbol.SymbolClassCode.CONST.ordinal());
        firstSetOfDeclaration.set(Symbol.SymbolClassCode.VAR.ordinal());
        firstSetOfDeclaration.set(Symbol.SymbolClassCode.PROCEDURE.ordinal());

        /**
         * 设置语句开始符号集
         * <语句> ::=<赋值语句>|<条件语句>|<当型循环语句>|<过程调用语句>|<读语句>|<写语句>|<复合语句>|<重复语句>|<空>
         * <赋值语句> ::= <标识符>:=<表达式>
         * <条件语句> ::= if<条件>then<语句>[else<语句>]
         * <当型循环语句> ::= while<条件>do<语句>
         * <重复语句> ::= repeat<语句>{;<语句>}until<条件>
         * <过程调用语句> ::= call<标识符>
         * <复合语句> ::= begin<语句>{;<语句>}end
         * FIRST(statement)={begin call if while repeat null};
         */
        firstSetOfStatement = new BitSet(Symbol.SymbolClassCode.values().length);
        firstSetOfStatement.set(Symbol.SymbolClassCode.BEGIN.ordinal());
        firstSetOfStatement.set(Symbol.SymbolClassCode.CALL.ordinal());
        firstSetOfStatement.set(Symbol.SymbolClassCode.IF.ordinal());
        firstSetOfStatement.set(Symbol.SymbolClassCode.WHILE.ordinal());
        firstSetOfStatement.set(Symbol.SymbolClassCode.REPEAT.ordinal());
        //TODO 添加读语句、写语句到first集

        /**
         * 设置因子开始符号集
         * <因子> ::= <标识符>|<无符号整数>|'('<表达式>')'
         * FIRST(factor)={identifier,number,(};
         */
        firstSetOfFactor = new BitSet(Symbol.SymbolClassCode.values().length);
        firstSetOfFactor.set(Symbol.SymbolClassCode.IDENTIFIER.ordinal());
        firstSetOfFactor.set(Symbol.SymbolClassCode.NUMBER.ordinal());
        firstSetOfFactor.set(Symbol.SymbolClassCode.LEFT_PARENTHESIS.ordinal());
    }

    /**
     * 获取下一个符号
     */
    private void nextSymbol() throws IOException {
        currentSymbol = lexicalScanner.getSymbol();
    }

    /**
     * 开始语法分析程序
     * <程序>::=<分程序>.
     * <p/>
     * 使用前需要调用nextSymbol()
     */
    public void parse() throws IOException {

        if (currentSymbol == null)//保证调用时currentSymbol不为空
            nextSymbol();

        // <分程序>的follow集 {. ;}
        //TODO 和sample不同
        BitSet follows = new BitSet(Symbol.SymbolClassCode.values().length);
        follows.set(Symbol.SymbolClassCode.PERIOD.ordinal());
        follows.set(Symbol.SymbolClassCode.SEMICOLON.ordinal());

        block(follows, 0);//<分程序>

        if (currentSymbol.getSymbolClassCode() != Symbol.SymbolClassCode.PERIOD) {
            errorHandler.printError(9, lexicalScanner.getCurrentLineNumber());//缺少句号
        }

        symbolTable.printTable();//打印符号表内所有信息
        interpreter.printPCodes();//打印生成的PCode
    }

    /**
     * <分程序>处理函数
     * <分程序>::=[<常量说明部分>][<变量说明部分>][<过程说明部分>]<语句>
     *
     * @param follows 当前模块的FOLLOW集合
     * @param level   当前程序块所在level
     */
    private void block(BitSet follows, int level) throws IOException {

        BitSet next = null;//TODO 作用不明

        //TODO 不明
        int origionDataAllocationIndex = dataAllocationIndex;//记录本层之前的数据量，以便返回时恢复
        int origionTableIndex = symbolTable.getTableIndex();
        int origionCodeIndex;

        //每层最开始的位置有三个空间用于存放静态链SL、动态链DL、返回地址RA
        dataAllocationIndex = 3;//TODO 上述原因不明
        //设置符号表当前项的address为当前pcode代码地址.在符号表当前位置记录下jmp指令在代码段中的位置
        symbolTable.getTupleAtIndex(symbolTable.getTableIndex()).address = interpreter.getCodeIndex();
        interpreter.genPCode(PCode.CodeType.JMP, 0, 0);

        if (level > SymbolTable.MAX_LEVEL) {
            errorHandler.printError(32, lexicalScanner.getCurrentLineNumber());//嵌套层数过大
            //TODO 是否需要终止/return
        }

        //分析<说明部分>
        do {
            /**
             * 分析 <常量说明部分> ::= const<常量定义>{,<常量定义>};
             */
            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.CONST) {
                nextSymbol();
                constantDeclaration(level);//分析 <常量定义>
                //处理 {,<常量定义>}
                while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.COMMA) {
                    nextSymbol();
                    constantDeclaration(level);
                }


                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON) {//常量声明结束
                    nextSymbol();
                } else {
                    errorHandler.printError(5, lexicalScanner.getCurrentLineNumber());//缺少逗号或分号
                }
            }

            /**
             * 分析 <变量说明部分> ::= var<标识符>{,<标识符>};
             */
            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.VAR) {
                nextSymbol();
                variableDeclaration(level);
                while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.COMMA) {
                    nextSymbol();
                    variableDeclaration(level);
                }

                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON) {//常量声明结束
                    nextSymbol();
                } else {
                    errorHandler.printError(5, lexicalScanner.getCurrentLineNumber());//缺少逗号或分号
                }
            }

            /**
             * 分析 <过程说明部分> ::=  procedure<标识符>; <分程序>;
             * FOLLOW(semicolon)={ NULL <过程首部> }
             * 需要进行test procedure a1; procedure 允许嵌套，故用while
             */

            while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.PROCEDURE) {
                //TODO
                nextSymbol();
                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.IDENTIFIER) {
                    symbolTable.enterProcedure(currentSymbol.getToken(), level);
                    //TODO 是否需要 dx++;
                    nextSymbol();
                } else
                    errorHandler.printError(4, lexicalScanner.getCurrentLineNumber());//procedure之后应为标识符

                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON) {
                    nextSymbol();
                } else
                    errorHandler.printError(5, lexicalScanner.getCurrentLineNumber());//缺少逗号或分号

                BitSet blockFollow = (BitSet) follows.clone();//block的follow集合
                blockFollow.set(Symbol.SymbolClassCode.SEMICOLON.ordinal());//follow(block) = { ; }
                //分析 <分程序>
                block(blockFollow, level + 1);

                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON) {//<过程说明部分>识别完成
                    nextSymbol();

                    next = (BitSet) firstSetOfStatement.clone();//将next设置为statement的first集
                    //FOLLOW(嵌套分程序)={ identifier , procedure } //TODO 是否包含const
                    next.set(Symbol.SymbolClassCode.IDENTIFIER.ordinal());
                    next.set(Symbol.SymbolClassCode.PROCEDURE.ordinal());

                    test(next, follows, 6);//测试current symbol属于statement的first集，否则报错：过程说明后的符号不正确
                } else
                    errorHandler.printError(5, lexicalScanner.getCurrentLineNumber());//缺少逗号或分号
            }

            //一个分程序的说明部分识别结束后，下面可能是statement 或者 声明部分
            //FIRST(statement)={begin call if while repeat null };
            next = (BitSet) firstSetOfStatement.clone();
            //first(statement)还包含identifier //TODO 不明
            next.set(Symbol.SymbolClassCode.IDENTIFIER.ordinal());

            test(next, firstSetOfDeclaration, 7);//测试是否为statement
        } while (firstSetOfDeclaration.get(currentSymbol.getSymbolClassCode().ordinal()));//直到不在声明的first集内

        //开始生成当前过程代码
        /**
         * 说明部分分析完后，开始分析<语句>
         * 此时代码分配指针(code allocation index)刚好指向语句的开始位置  //TODO 不明
         * 此位置正是前面JMP指令需要跳转到的位置
         */
        Tuple tuple = symbolTable.getTupleAtIndex(origionTableIndex);
        interpreter.getPCodeAtIndex(tuple.address).setArgument(interpreter.getCodeIndex());//TODO 不确定写法是否正确
        tuple.address = interpreter.getCodeIndex();
        tuple.size = dataAllocationIndex;//一个procedure中的变量数目+3 ，声明部分中每增加一条声明都会给dx+1
        //声明部分已经结束，此时data allocation index是当前过程的堆栈帧大小
        /**
         * 把JMP指令的跳转位置改成当前code index的位置。
         * 并在符号表中记录下当前的代码段分配地址和局部数据段要分配的大小(data allocation index的值).
         * 生成一条int指令，分配data allocation index个空间，作为这个分程序段的第一条指令。
         * 然后调用语句处理过程statement分析语句。
         */
        origionCodeIndex = interpreter.getCodeIndex();
        //生成分配内存代码
        interpreter.genPCode(PCode.CodeType.INT, 0, dataAllocationIndex);

        //打印 说明部分 代码
        symbolTable.printTable(origionTableIndex);


        //分析 <语句>
        next = (BitSet) follows.clone();//每个FOLLOW集合都包含上层FOLLOW集合，以便补救
        next.set(Symbol.SymbolClassCode.SEMICOLON.ordinal());
        next.set(Symbol.SymbolClassCode.END.ordinal());

        statement(next, level);

        /**
         * 分析完成后，生成操作数为0的OPR指令，用于从分程序返回(对于0层的主程序来说，就是程序运行完成，退出)。
         */
        interpreter.genPCode(PCode.CodeType.OPR, 0, 0);


        next = new BitSet(Symbol.SymbolClassCode.values().length);
        test(follows, next, 8);//检测之后符号的正确性

        interpreter.printPCodes(origionCodeIndex);

        dataAllocationIndex = origionDataAllocationIndex;//恢复堆栈指针计数器
        symbolTable.setTableIndex(origionTableIndex);//恢复符号表位置
    }

    /**
     * <常量定义>处理函数
     * <常量定义>::=<标识符>=<无符号整数>
     *
     * @param level 当前模块所在的层次
     */
    private void constantDeclaration(int level) throws IOException {
        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.IDENTIFIER) {//符合常量定义的语法规定
            String identifier = currentSymbol.getToken();
            nextSymbol();
            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.EQUAL ||
                    currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.ASSIGN) {
                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.ASSIGN) {
                    //将=写为了:=，只提示错误信息，仍继续处理
                    errorHandler.printError(1, lexicalScanner.getCurrentLineNumber());
                }

                nextSymbol();
                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.NUMBER) {
                    currentSymbol.setToken(identifier);//将常量与标识符绑定
                    symbolTable.enterConstant(currentSymbol.getToken(), currentSymbol.getValue());//将常量填入符号表
                    nextSymbol();
                } else
                    errorHandler.printError(2, lexicalScanner.getCurrentLineNumber());//按照语法应该为无符号整数

            } else
                errorHandler.printError(3, lexicalScanner.getCurrentLineNumber());//按照语法此处（标识符之后）应该为=

        } else {//常量定义应由标识符开始
            errorHandler.printError(4, lexicalScanner.getCurrentLineNumber());
        }
    }


    /**
     * <标识符>处理函数
     * <变量说明部分>::=var<标识符>{,<标识符>};
     *
     * @param level
     * @throws IOException
     */
    private void variableDeclaration(int level) throws IOException {
        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.IDENTIFIER) {

            //填写符号表并改变堆栈帧计数器 符号表中记录下标识符的名字、它所在的层及它在所在层中的偏移地址
            symbolTable.enterVariable(currentSymbol.getToken(), level, dataAllocationIndex);
            dataAllocationIndex++;

            nextSymbol();
        } else {
            errorHandler.printError(4, lexicalScanner.getCurrentLineNumber());//var之后应该是标识符
        }
    }

    /**
     * <语句>::=<赋值语句>|<条件语句>|<当型循环语句>|<过程调用语句>|<读语句>|<写语句>|<复合语句>|<重复语句>|<空>
     *
     * @param follows Follow结合
     * @param level   当前层级
     */
    private void statement(BitSet follows, int level) throws IOException {
        // FIRST(statement)={ identifier, read, write, call, if, while, repeat, begin}
        switch (currentSymbol.getSymbolClassCode()) {
            case IDENTIFIER:
                assignStatement(follows, level);
            case READ:
                readStatement(follows, level);
            case WRITE:
                writeStatement(follows, level);
            case CALL:
                callStatement(follows, level);
            case IF:
                ifStatement(follows, level);
            case BEGIN:
                beginStatement(follows, level);
            case WHILE:
                whileStatement(follows, level);
            case REPEAT:
                repeatStatement(follows, level);
            default: {
                BitSet statementFollows = new BitSet(Symbol.SymbolClassCode.values().length);
                test(follows, statementFollows, 19);//语句后的符号不正确
                break;
            }
        }
    }

    /**
     * <重复语句>::=repeat<语句>{;<语句>}until<条件>
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void repeatStatement(BitSet follows, int level) throws IOException {
        //获取指令索引指针，即cx
        int codeIndexPointer = interpreter.getCodeIndex();
        nextSymbol();

        BitSet subFollows = (BitSet) follows.clone();
        subFollows.set(Symbol.SymbolClassCode.SEMICOLON.ordinal());
        subFollows.set(Symbol.SymbolClassCode.UNTIL.ordinal());
        statement(follows, level);

        //处理{,<语句>}
        while (firstSetOfStatement.get(currentSymbol.getSymbolClassCode().ordinal()) ||
                currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON) {
            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON)
                nextSymbol();
            else
                errorHandler.printError(5, lexicalScanner.getCurrentLineNumber());//漏掉分号

            statement(subFollows, level);
        }
        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.UNTIL) {
            nextSymbol();
            condition(follows, level);
            interpreter.genPCode(PCode.CodeType.JPC, 0, codeIndexPointer);
        } else {
            //TODO 未定义错误信息
            errorHandler.printError(19, lexicalScanner.getCurrentLineNumber());//缺少until语句
        }
    }


    /**
     * <当型循环语句>::=while<条件>do<语句>
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void whileStatement(BitSet follows, int level) throws IOException {

        int conditionCodeIndexPointer = interpreter.getCodeIndex();//保存<条件>操作的位置
        nextSymbol();

        BitSet conditionFollow = (BitSet) follows.clone();
        conditionFollow.set(Symbol.SymbolClassCode.DO.ordinal());
        condition(conditionFollow, level);//<条件>

        int endCodeIndexPointer = interpreter.getCodeIndex();//保存循环结束的下一个位置
        interpreter.genPCode(PCode.CodeType.JPC, 0, 0);

        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.DO) {
            nextSymbol();
        } else {
            errorHandler.printError(18, lexicalScanner.getCurrentLineNumber());//缺少do语句
        }

        statement(follows, level);//<语句>

        interpreter.genPCode(PCode.CodeType.JMP, 0, conditionCodeIndexPointer);//跳转，并重新判断是否符合条件
        //回填跳出循环的地址
        PCode code = interpreter.getPCodeAtIndex(endCodeIndexPointer);
        code.setArgument(interpreter.getCodeIndex());
        interpreter.setPCodeAtIndex(endCodeIndexPointer, code);
    }

    /**
     * 处理<复合语句>
     * <复合语句>::=begin<语句>{;<语句>}end
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void beginStatement(BitSet follows, int level) throws IOException {

        nextSymbol();

        BitSet statementFollows = (BitSet) follows.clone();
        statementFollows.set(Symbol.SymbolClassCode.SEMICOLON.ordinal());
        statementFollows.set(Symbol.SymbolClassCode.END.ordinal());

        statement(statementFollows, level);

        while (firstSetOfStatement.get(currentSymbol.getSymbolClassCode().ordinal()) ||
                currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON) {
            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.SEMICOLON)
                nextSymbol();
            else
                errorHandler.printError(10, lexicalScanner.getCurrentLineNumber());//缺少分号
            statement(statementFollows, level);
        }

        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.END) {
            nextSymbol();
        } else {
            errorHandler.printError(17, lexicalScanner.getCurrentLineNumber());//缺少end或分号
        }
    }

    /**
     * <条件语句>
     * <条件语句>::=if<条件>then<语句>[else<语句>]
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void ifStatement(BitSet follows, int level) throws IOException {

        nextSymbol();

        BitSet conditionFollows = (BitSet) follows.clone();
        //FOLLOW(condition)={ then do }     <当型循环语句> ::= while<条件>do<语句>
        conditionFollows.set(Symbol.SymbolClassCode.THEN.ordinal());
        conditionFollows.set(Symbol.SymbolClassCode.DO.ordinal());

        condition(conditionFollows, level);//<条件>

        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.THEN) {
            nextSymbol();
        } else {
            errorHandler.printError(16, lexicalScanner.getCurrentLineNumber());//缺少then
        }

        int codeIndexPointer = interpreter.getCodeIndex();
        interpreter.genPCode(PCode.CodeType.JMP, 0, 0);//生成跳转指令，跳转地址暂时记为0
        statement(follows, level);//<语句>

        //回填跳转地址
        PCode code = interpreter.getPCodeAtIndex(codeIndexPointer);
        code.setArgument(interpreter.getCodeIndex());
        interpreter.setPCodeAtIndex(codeIndexPointer, code);

        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.ELSE) {
            code.setArgument(interpreter.getCodeIndex() + 1);
            interpreter.setPCodeAtIndex(codeIndexPointer, code);

            nextSymbol();
            int tempIndex = interpreter.getCodeIndex();
            interpreter.genPCode(PCode.CodeType.JMP, 0, 0);
            statement(follows, level);

            PCode temp = interpreter.getPCodeAtIndex(tempIndex);
            temp.setArgument(interpreter.getCodeIndex());
            interpreter.setPCodeAtIndex(tempIndex, temp);
        }
    }

    /**
     * 处理<过程调用语句>
     * <过程调用语句>::=call<标识符>
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void callStatement(BitSet follows, int level) throws IOException {
        nextSymbol();


        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.IDENTIFIER)//<标识符>
        {
            int index = symbolTable.position(currentSymbol.getToken());//该标识符在符号表中的位置
            if (index >= 0) {

                Tuple tuple = symbolTable.getTupleAtIndex(index);
                if (tuple.kind == Tuple.TupleType.PROCEDURE) {
                    interpreter.genPCode(PCode.CodeType.CAL, level - tuple.level, tuple.address);
                } else
                    errorHandler.printError(15, lexicalScanner.getCurrentLineNumber());//只能调用procedure


            } else
                errorHandler.printError(11, lexicalScanner.getCurrentLineNumber());//过程标识符未声明

            nextSymbol();
        } else
            errorHandler.printError(14, lexicalScanner.getCurrentLineNumber());//call 后面应该为标识符

    }


    /**
     * <写语句>处理函数
     * <写语句>::=write'('<表达式>{,<表达式>}')'
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void writeStatement(BitSet follows, int level) throws IOException {
        nextSymbol();

        BitSet expressionFollow = (BitSet) follows.clone();
        expressionFollow.set(Symbol.SymbolClassCode.COMMA.ordinal());
        expressionFollow.set(Symbol.SymbolClassCode.RIGHT_PARENTHESIS.ordinal());

        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.LEFT_PARENTHESIS) {
            do {
                nextSymbol();
                expression(expressionFollow, level);//<表达式>   已经包含nextSymbol，不需要继续取元素
                interpreter.genPCode(PCode.CodeType.OPR, 0, 14);//输出栈顶的值
            } while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.COMMA);

            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.RIGHT_PARENTHESIS) {
                nextSymbol();
            } else
                errorHandler.printError(33, lexicalScanner.getCurrentLineNumber());//缺少右括号
        } else
            errorHandler.printError(34, lexicalScanner.getCurrentLineNumber());//缺少左括号

        interpreter.genPCode(PCode.CodeType.OPR, 0, 15);//输出换行
    }

    /**
     * <读语句>处理函数
     * <读语句>::=read'('<标识符>{,<标识符>}')'
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void readStatement(BitSet follows, int level) throws IOException {

        nextSymbol();

        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.LEFT_PARENTHESIS) {

            do {
                nextSymbol();//读入<标识符>

                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.IDENTIFIER) {
                    int index = symbolTable.position(currentSymbol.getToken());//查表

                    if (index > -1) {

                        Tuple tuple = symbolTable.getTupleAtIndex(index);

                        if (tuple.kind != Tuple.TupleType.VARIABLE) {
                            interpreter.genPCode(PCode.CodeType.OPR, 0, 16);//读入一个数据
                            interpreter.genPCode(PCode.CodeType.STO, level - tuple.level, tuple.address);// 存储变量
                        } else
                            errorHandler.printError(32, lexicalScanner.getCurrentLineNumber());//应该为变量

                    } else
                        errorHandler.printError(35, lexicalScanner.getCurrentLineNumber());//read()中的变量未声明

                    nextSymbol();//读入,
                }
            } while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.COMMA);


            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.RIGHT_PARENTHESIS) {//匹配完成
                nextSymbol();
            } else {
                errorHandler.printError(33, lexicalScanner.getCurrentLineNumber());
                while (!follows.get(currentSymbol.getSymbolClassCode().ordinal()))
                    nextSymbol();
            }
        } else {
            errorHandler.printError(34, lexicalScanner.getCurrentLineNumber());//应为左括号
        }
    }

    /**
     * <赋值语句>::=<标识符>:=<表达式>
     *
     * @param follows Follow集合
     * @param level   当前层级
     */
    private void assignStatement(BitSet follows, int level) throws IOException {

        //从符号表中查找当前标识符
        int index = symbolTable.position(currentSymbol.getToken());
        if (index > 0) {
            Tuple tuple = symbolTable.getTupleAtIndex(index);
            if (tuple.kind == Tuple.TupleType.VARIABLE)//<标识符>
            {
                nextSymbol();
                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.ASSIGN) {
                    nextSymbol();
                } else
                    errorHandler.printError(13, lexicalScanner.getCurrentLineNumber());//未检测到赋值符号

                expression((BitSet) follows.clone(), level);//<表达式>
                //将expression所得结果（栈顶）赋值到<标识符>对应的地址中
                interpreter.genPCode(PCode.CodeType.STO, level - tuple.level, tuple.address);
            } else
                errorHandler.printError(12, lexicalScanner.getCurrentLineNumber());//不可向常量或过程名赋值
        } else
            errorHandler.printError(11, lexicalScanner.getCurrentLineNumber());//标识符未声明
    }

    /**
     * <条件>处理函数
     * <条件>::=<表达式><关系运算符><表达式>|odd<表达式>
     * 首先判断是否为一元逻辑表达式：判奇偶。 如果是，则通过调用表达式处理过程分析计算表达式的值， 然后生成判奇指令。
     * 如果不是，则肯定是二元逻辑运算符， 通过调用表达式处理过程依次分析运算符左右两部分的值， 放在栈顶的两个空间中，然后依不同的逻辑运算符，
     * 生成相应的逻辑判断指令，放入代码段。
     *
     * @param follows Follow集合
     * @param level   当前层次
     */
    private void condition(BitSet follows, int level) throws IOException {
        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.ODD) {
            nextSymbol();
            expression(follows, level);
            interpreter.genPCode(PCode.CodeType.OPR, 0, 6); //OPR 0 6:判断栈顶元素是否为奇数
        } else {//处理<表达式><关系运算符><表达式>的情况
            //FOLLOW(expression)={ = != < <= > >= }
            BitSet expressionFollow = (BitSet) follows.clone();
            expressionFollow.set(Symbol.SymbolClassCode.EQUAL.ordinal());
            expressionFollow.set(Symbol.SymbolClassCode.NOT_EQUAL.ordinal());
            expressionFollow.set(Symbol.SymbolClassCode.LESS_THAN.ordinal());
            expressionFollow.set(Symbol.SymbolClassCode.LESS_THAN_OR_EQUAL.ordinal());
            expressionFollow.set(Symbol.SymbolClassCode.GREATER_THAN.ordinal());
            expressionFollow.set(Symbol.SymbolClassCode.GREATER_THAN_OR_EQUAL.ordinal());

            expression(expressionFollow, level);

            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.EQUAL ||
                    currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.NOT_EQUAL ||
                    currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.LESS_THAN_OR_EQUAL ||
                    currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.LESS_THAN ||
                    currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.GREATER_THAN ||
                    currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.GREATER_THAN_OR_EQUAL) {

                int relationOperator = currentSymbol.getSymbolClassCode().ordinal();
                nextSymbol();
                expression(follows, level);
                /**
                 * SymbolClassCode的Equal到Less_Than_Or_Equal与8到13对应
                 */
                interpreter.genPCode(PCode.CodeType.OPR, 0, relationOperator);
            } else {
                errorHandler.printError(20, lexicalScanner.getCurrentLineNumber());//应为关系运算符
            }
        }
    }

    /**
     * <表达式>处理函数
     * <表达式>::= [+|-]<项>{<加法运算符><项>}
     * 根据PL/0语法可知，表达式应该是由正负号或无符号开头、由若干个项以加减号连接而成。 而项是由若干个因子以乘除号连接而成， 因子则可能是一个标识符或一个数字，
     * 或是一个以括号括起来的子表达式。 根据这样的结构，构造出相应的过程， 递归调用就完成了表达式的处理。
     * 把项和因子独立开处理解决了加减号与乘除号的优先级问题。 在这几个过程的反复调用中，始终传递fsys变量的值，
     * 保证可以在出错的情况下跳过出错的符号，使分析过程得以进行下去
     *
     * @param follows
     * @param level
     */
    private void expression(BitSet follows, int level) throws IOException {
        if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.PLUS ||
                currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.MINUS) {

            Symbol.SymbolClassCode addOperatorType = currentSymbol.getSymbolClassCode();

            BitSet termFollows = (BitSet) follows.clone();
            termFollows.set(Symbol.SymbolClassCode.PLUS.ordinal());
            termFollows.set(Symbol.SymbolClassCode.MINUS.ordinal());

            term(termFollows, level);

            if (addOperatorType == Symbol.SymbolClassCode.MINUS) {//取反，如果是加号，不需要处理
                interpreter.genPCode(PCode.CodeType.OPR, 0, 1);//1表示negative
            }
        } else//为<项>
        {
            BitSet termFollows = (BitSet) follows.clone();
            termFollows.set(Symbol.SymbolClassCode.PLUS.ordinal());
            termFollows.set(Symbol.SymbolClassCode.MINUS.ordinal());

            term(termFollows, level);
        }

        //{<加法运算符><项>}
        while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.PLUS ||
                currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.MINUS) {
            int addOperatorType = currentSymbol.getSymbolClassCode().ordinal();
            nextSymbol();
            BitSet termFollows = (BitSet) follows.clone();
            termFollows.set(Symbol.SymbolClassCode.PLUS.ordinal());
            termFollows.set(Symbol.SymbolClassCode.MINUS.ordinal());

            term(termFollows, level);

            interpreter.genPCode(PCode.CodeType.OPR, 0, addOperatorType);//2表示加法，3表示减法
        }
    }

    /**
     * <项>处理函数
     * <项> ::= <因子>{<乘法运算符><因子>}
     *
     * @param follows Follow集合
     * @param level   所在层次
     */
    private void term(BitSet follows, int level) throws IOException {
        //<因子>
        BitSet factorFollows = (BitSet) follows.clone();
        factorFollows.set(Symbol.SymbolClassCode.MULTIPLY.ordinal());
        factorFollows.set(Symbol.SymbolClassCode.DIVIDE.ordinal());

        factor(factorFollows, level);

        //{<乘法运算符><因子>}
        while (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.MULTIPLY ||
                currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.DIVIDE) {
            int multiplyOperatorType = currentSymbol.getSymbolClassCode().ordinal();
            nextSymbol();
            factor(factorFollows, level);
            interpreter.genPCode(PCode.CodeType.OPR, 0, multiplyOperatorType);//4为乘法，5为除法
        }
    }

    /**
     * <因子>处理函数
     * <因子>=<标识符>|<无符号整数>|'('<表达式>')'
     * <p/>
     * 开始因子处理前，先检查当前token是否在factor的first集合中。
     * 如果不是合法的token，抛24号错误，并通过follows集恢复使语法处理可以继续进行
     *
     * @param follows Follow集合
     * @param level   所在层次
     */
    private void factor(BitSet follows, int level) throws IOException {
        test(firstSetOfFactor, follows, 24);//检测因子的开始符号

        if (firstSetOfFactor.get(currentSymbol.getSymbolClassCode().ordinal())) {
            if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.IDENTIFIER)//<标识符>
            {
                int index = symbolTable.position(currentSymbol.getToken());
                if (index >= 0) {//符号表中存在
                    Tuple tuple = symbolTable.getTupleAtIndex(index);
                    switch (tuple.kind) {
                        case CONSTANT:
                            interpreter.genPCode(PCode.CodeType.LIT, 0, tuple.value);//生成lit指令，把这个数值字面常量放到栈顶
                            break;
                        case VARIABLE:
                            interpreter.genPCode(PCode.CodeType.LOD, level - tuple.level, tuple.address);//把位于距离当前层level的层的偏移地址为adr的变量放到栈顶
                            break;
                        case PROCEDURE:
                            errorHandler.printError(21, lexicalScanner.getCurrentLineNumber());//标识符内不可有过程标识符
                            break;
                    }
                } else
                    errorHandler.printError(11, lexicalScanner.getCurrentLineNumber());//标识符未声明

                nextSymbol();
            } else if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.NUMBER) {//<无符号整数>
                int num = currentSymbol.getValue();

                if (num > SymbolTable.MAX_NUMBER) {
                    errorHandler.printError(31, lexicalScanner.getCurrentLineNumber());//数字超过最大值
                    num = 0;
                }
                interpreter.genPCode(PCode.CodeType.LIT, 0, num);//把常数放到栈顶
            } else if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.LEFT_PARENTHESIS) {//'('<表达式>')'
                nextSymbol();
                BitSet expressionFollows = (BitSet) follows.clone();
                expressionFollows.set(Symbol.SymbolClassCode.RIGHT_PARENTHESIS.ordinal());

                expression(expressionFollows, level);

                if (currentSymbol.getSymbolClassCode() == Symbol.SymbolClassCode.RIGHT_PARENTHESIS) {//匹配完成
                    nextSymbol();
                } else {
                    errorHandler.printError(22, lexicalScanner.getCurrentLineNumber());//缺少右括号
                }
            } else {//补救措施
                test(follows, firstSetOfFactor, 23);//如果不是，报错，并找到下一个因子的开始，使语法分析程序继续运行
            }
        }
    }

    /**
     * 在每个语言分析子程序出口处，检测下一个取来的符号是否为该语法成分的合法后继符号。
     * 若不是，则应报告出错信息，并且跳读一段源程序，直至取来的符号属于该语法成分的合法后继符号集合为止
     * <p/>
     * 主要用法:
     * 在进入某个语法单位时，调用本过程， 检查当前符号是否属于该语法单位的头符号集合。 若不属于，则滤去开始符号和后继符号集合外的所有符号。
     * 在语法单位分析结束时，调用本过程， 检查当前符号是否属于调用该语法单位时应有的后继符号集合。 若不属于，则滤去后继符号和开始符号集合外的所有符号。
     * 通过这样的机制，可以在源程序出现错误时， 及时跳过出错的部分，保证语法分析可以继续下去。
     *
     * @param follows   合法的follow集
     * @param stops     停止符号集
     * @param errorCode 错误编码
     * @throws IOException
     */
    private void test(BitSet follows, BitSet stops, int errorCode) throws IOException {
        if (!follows.get(currentSymbol.getSymbolClassCode().ordinal())) {
            errorHandler.printError(errorCode, lexicalScanner.getCurrentLineNumber());

            follows.or(stops);//相当于follows + stops
            while (!follows.get(currentSymbol.getSymbolClassCode().ordinal()))
                nextSymbol();
        }
    }

}