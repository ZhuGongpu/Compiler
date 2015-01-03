package compiler;

import compiler.interpreter.Interpreter;
import compiler.lexical.Scanner;
import compiler.symbol_table.SymbolTable;
import compiler.syntax.Parser;

import java.io.*;

/**
 * Created by zhugongpu on 15/1/2.
 */
public class PL0Compiler {

    private static final String PCODE_FILE = "PCode";
    private static final String SYMBOL_TABLE_FILE = "SymbolTable";
    private static final String ERROR_FILE = "Error";
    private static final String RUNTIME_FILE = "Runtime";
    private static final String INPUT_FILE = "Input";

    private BufferedWriter pcodeWriter = null;//输出PCode
    private BufferedWriter runtimeWriter = null;//输出结果
    private BufferedWriter symbolTableWriter = null;//输出符号表
    private BufferedWriter errorWriter = null;

    private Parser parser = null;
    private Interpreter interpreter = null;


    public PL0Compiler(String filePath) throws FileNotFoundException {
        interpreter = new Interpreter();
        parser = new Parser(
                new Scanner(filePath),
                new SymbolTable(),
                interpreter
        );
    }

    /**
     * 编译
     *
     * @return 返回是否有错
     */
    public boolean compile() throws IOException {
        pcodeWriter = new BufferedWriter(new FileWriter(PCODE_FILE));
        runtimeWriter = new BufferedWriter(new FileWriter(RUNTIME_FILE));
        symbolTableWriter = new BufferedWriter(new FileWriter(SYMBOL_TABLE_FILE));
        errorWriter = new BufferedWriter(new FileWriter(ERROR_FILE));

        parser.parse();//开始语法分析过程（连同语法检查，目标代码生成）

        pcodeWriter.close();
        runtimeWriter.close();
        symbolTableWriter.close();
        errorWriter.close();


        return parser.getErrorCount() == 0;
    }


    public void interpret() throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(INPUT_FILE));
        BufferedWriter output = new BufferedWriter(new FileWriter(RUNTIME_FILE));
        interpreter.interpret(input, output);

        input.close();
        output.close();
    }
}
