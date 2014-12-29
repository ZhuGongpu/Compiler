package compiler;

import compiler.lexical.Scanner;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {

        try {
            new Main().test("./pl0.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void test(String filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(filePath);

    }
}
