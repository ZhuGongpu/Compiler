package gui;

import compiler.PL0Compiler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by zhugongpu on 15/1/3.
 */
public class MainForm {
    private static final String testFilePath = "./src/pl0.txt";
    private JTextArea sourceProgram;
    private JTextArea pCode;

    private JButton compileButton;
    private JPanel mainPanel;
    private JProgressBar progressBar;
    /**
     * 将p-code重定向到 text area
     */
    private PrintStream pCodePrintStream = new PrintStream(System.out) {

        @Override
        public void write(final byte[] buf, final int off, final int len) {
            super.write(buf, off, len);
            System.err.print(new String(buf, off, len));
            pCode.append(new String(buf, off, len));

//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
        }
    };

    public MainForm() {

        progressBar.setVisible(false);

        compileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (sourceProgram.getText().length() == 0) {
                    //TODO Alert 不能为空
                    AlertDialog dialog = new AlertDialog();
                    dialog.pack();
                    dialog.setVisible(true);

                    return;
                }

//                sourceProgram.setText("");
                pCode.setText("");

                //TODO display source program


                progressBar.setVisible(true);
                compileButton.setEnabled(false);

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            progressBar.setIndeterminate(true);

                            PL0Compiler compiler = new PL0Compiler(testFilePath);
                            System.out.println("compiling");
                            compiler.compile(pCodePrintStream);

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    compileButton.setEnabled(true);
                                    progressBar.setValue(100);
                                    progressBar.setIndeterminate(false);
                                    progressBar.setString("Done");
//                            progressBar.setVisible(false);

                                }
                            });
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    }
                }.start();
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

}
