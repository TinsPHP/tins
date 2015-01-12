/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.tinsphp.demo;


import ch.tsphp.common.ICompilerListener;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.HardCodedCompilerInitialiser;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import org.antlr.runtime.RecognitionException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;

public class CompilerDemo extends JFrame implements ICompilerListener, IIssueLogger
{

    private final ICompiler compiler;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    /**
     * Creates new form CompilerDemo
     */
    public CompilerDemo() {
        initComponents();
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        String path = "./bin/tinsphp.png";
        setIconImage(new ImageIcon(path).getImage());

        compiler = new HardCodedCompilerInitialiser().create(Executors.newSingleThreadExecutor());
        compiler.registerCompilerListener(this);
        compiler.registerIssueLogger(this);
    }

    /**
     * This method is called from within the constructor to initialise the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TinsPHP Demonstration");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        initMenuBar();

        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();

        txtPHP = new RSyntaxTextArea(20, 60);
        txtPHP.setText("<?php\n\n?>");
        txtPHP.setCaretPosition(6);
        txtPHP.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        txtPHP.setCodeFoldingEnabled(true);

        RTextScrollPane scrollPHP = new RTextScrollPane(txtPHP);
        scrollPHP.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int size = validateSize(txtPHP.getFont().getSize() - e.getWheelRotation());
                    txtPHP.setFont(new Font("Consolas", Font.PLAIN, size));
                }
            }
        });

        txtTSPHP = new RSyntaxTextArea(20, 60);
        txtTSPHP.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        txtTSPHP.setCodeFoldingEnabled(false);
        RTextScrollPane scrollTSPHP = new RTextScrollPane(txtTSPHP);
        scrollTSPHP.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int size = validateSize(txtTSPHP.getFont().getSize() - e.getWheelRotation());
                    txtTSPHP.setFont(new Font("Consolas", Font.PLAIN, size));
                }
            }
        });


        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/vs.xml"));
            theme.apply(txtPHP);
            theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/vs.xml"));
            theme.apply(txtTSPHP);
        } catch (Exception ex) {
            //well then, no dark theme for us
        }
        Font font = new Font("Consolas", Font.PLAIN, 18);
        txtPHP.setFont(font);
        txtTSPHP.setFont(font);

        txtOutput = new javax.swing.JTextArea();
        txtOutput.setFont(font);

        jScrollPane2 = new javax.swing.JScrollPane();
        jSplitPane2.setDividerLocation(500);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setPreferredSize(new java.awt.Dimension(700, 600));

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(339, 500));

        jSplitPane1.setRightComponent(scrollTSPHP);
        jSplitPane1.setLeftComponent(scrollPHP);
        jSplitPane2.setLeftComponent(jSplitPane1);

        txtOutput.setColumns(20);
        txtOutput.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtOutput.setRows(5);
        jScrollPane2.setViewportView(txtOutput);
        jScrollPane2.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int size = validateSize(txtOutput.getFont().getSize() - e.getWheelRotation());
                    txtOutput.setFont(new Font("Consolas", Font.PLAIN, size));
                }
            }
        });

        jSplitPane2.setRightComponent(jScrollPane2);

        getContentPane().add(jSplitPane2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private int validateSize(int size) {
        if (size < 8) {
            size = 8;
        }
        if (size > 50) {
            size = 50;
        }
        return size;
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu mFile = new JMenu("File");
        mFile.setMnemonic(KeyEvent.VK_F);
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.setMnemonic(KeyEvent.VK_X);
        miExit.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(CompilerDemo.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        mFile.add(miExit);
        menuBar.add(mFile);

        JMenu mRun = new JMenu("Run");
        mRun.setMnemonic(KeyEvent.VK_R);
        JMenuItem miTranslate = new JMenuItem("Translate");
        miTranslate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        miTranslate.setMnemonic(KeyEvent.VK_T);
        miTranslate.getAccessibleContext().setAccessibleDescription("Translates PHP to TSPHP");
        miTranslate.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                translate();
            }
        });
        mRun.add(miTranslate);
        menuBar.add(mRun);
        setJMenuBar(menuBar);
    }

    private void translate() {
        if (!compiler.isCompiling()) {
            String php = txtPHP.getText();
            if (!php.isEmpty()) {
                compiler.reset();
                txtOutput.setText("");
                txtTSPHP.setText("");
                compiler.addCompilationUnit("demo", php);
                compiler.compile();
            } else {
                JOptionPane.showMessageDialog(this, "Please provide some code, otherwise it is quite boring ;-)");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please be patient, compilation is still ongoing");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing
                .UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CompilerDemo.class.getName()).log(java.util.logging.Level.SEVERE,
                    null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run() {
                new CompilerDemo().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTextArea txtOutput;
    private RSyntaxTextArea txtPHP;
    private RSyntaxTextArea txtTSPHP;
    // End of variables declaration//GEN-END:variables

    @Override
    public void afterParsingAndDefinitionPhaseCompleted() {
        txtOutput.append(
                dateFormat.format(new Date()) + ": Parsing and definition phase completed\n"
                        + "----------------------------------------------------------------------\n");
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }

    @Override
    public void afterReferencePhaseCompleted() {
        txtOutput.append(
                "\n" + dateFormat.format(new Date()) + ": Reference phase completed\n"
                        + "----------------------------------------------------------------------\n");
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }

    @Override
    public void afterTypecheckingCompleted() {
        txtOutput.append(
                "\n" + dateFormat.format(new Date()) + ": Inference phase completed\n"
                        + "----------------------------------------------------------------------\n");
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }

    @Override
    public void afterCompilingCompleted() {
        txtOutput.append("\n" + dateFormat.format(new Date()) + ": Compilation completed\n");

        Map<String, String> translations = compiler.getTranslations();
        txtTSPHP.setText(translations.get("demo"));
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }

    @Override
    public void log(TSPHPException exception, EIssueSeverity severity) {
        txtOutput.append(dateFormat.format(new Date()) + ": [" + severity + "] " + exception.getMessage() + "\n");
        Throwable throwable = exception.getCause();
        if (throwable != null && !(throwable instanceof RecognitionException)) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            txtOutput.append(stringWriter.toString());
        }
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }
}
