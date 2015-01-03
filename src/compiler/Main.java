package compiler;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    private static final String testFilePath = "./src/pl0.txt";

    public static void main(String[] args) {

//        File file = new File("./src/pl0.txt");
//        System.out.println(file.getAbsolutePath());
//
//        for (String s : file.list())
//            System.out.printf(s + "\n");

        try {
            new Main().test();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void test() throws IOException {

        PL0Compiler compiler = new PL0Compiler(testFilePath);

        System.out.println("compiling");
        compiler.compile();

        System.out.println("interpreting");
        compiler.interpret();
    }
}
