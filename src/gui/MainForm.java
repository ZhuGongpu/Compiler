package gui;

import compiler.PL0Compiler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * Created by zhugongpu on 15/1/3.
 */
public class MainForm {

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
            pCode.append(new String(buf, off, len));
        }
    };

    /**
     * 将error信息重定向
     */
    private PrintStream errorPrintStream = new PrintStream(System.out) {

        @Override
        public void write(final byte[] buf, final int off, final int len) {
            super.write(buf, off, len);

            pCode.append(new String(buf, off, len));
        }
    };

    public MainForm() {

        progressBar.setVisible(false);

        compileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (sourceProgram.getText().length() == 0) {
                    //Alert 不能为空
                    AlertDialog dialog = new AlertDialog();
                    dialog.pack();
                    dialog.setVisible(true);

                    return;
                }
                //清空PCode
                pCode.setText("");

                progressBar.setVisible(true);
                compileButton.setEnabled(false);

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            progressBar.setIndeterminate(true);

                            BufferedReader sourceProgramReader = new BufferedReader(new StringReader(sourceProgram.getText()));

                            PL0Compiler compiler = new PL0Compiler(sourceProgramReader, errorPrintStream);
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
