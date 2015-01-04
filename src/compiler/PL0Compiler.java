package compiler;

import compiler.interpreter.Interpreter;
import compiler.lexical.Scanner;
import compiler.symbol_table.SymbolTable;
import compiler.syntax.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by zhugongpu on 15/1/2.
 */
public class PL0Compiler {

    private Parser parser = null;


    public PL0Compiler(BufferedReader sourceProgram) throws FileNotFoundException {
        Interpreter interpreter = new Interpreter();
        parser = new Parser(
                new Scanner(sourceProgram),
                new SymbolTable(),
                interpreter
        );
    }

    /**
     * 编译
     *
     * @return 返回是否有错
     */
    public boolean compile(PrintStream pCodePrinter) throws IOException {
        parser.parse(pCodePrinter);//开始语法分析过程（连同语法检查，目标代码生成）
        return parser.getErrorCount() == 0;
    }

}
