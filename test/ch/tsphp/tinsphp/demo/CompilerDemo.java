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
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
        addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent evt) {
                formKeyReleased(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();

        txtPHP = new RSyntaxTextArea(20, 60);
        txtPHP.setText("<?php\n\n?>");
        txtPHP.setCaretPosition(6);
        Font font = new Font("Consolas", Font.PLAIN, 16);
        txtPHP.setFont(font);
        txtPHP.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        txtPHP.setCodeFoldingEnabled(true);
        txtPHP.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent e) {
                txtPHPKeyReleased(e);
            }
        });
        txtPHP.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    txtPHP.setFont(
                            new Font("Consolas", Font.PLAIN, txtPHP.getFont().getSize() - e.getWheelRotation()));
                }
            }
        });
        RTextScrollPane scrollPHP = new RTextScrollPane(txtPHP);

        txtTSPHP = new RSyntaxTextArea(20, 60);
        txtTSPHP.setFont(font);
        txtTSPHP.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        txtTSPHP.setCodeFoldingEnabled(false);
        txtTSPHP.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    txtTSPHP.setFont(
                            new Font("Consolas", Font.PLAIN, txtTSPHP.getFont().getSize() - e.getWheelRotation()));
                }
            }
        });
        RTextScrollPane scrollTSPHP = new RTextScrollPane(txtTSPHP);


        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/vs.xml"));
            theme.apply(txtPHP);
            theme.apply(txtTSPHP);
        } catch (Exception ex) {
            //well then, no dark theme for us
        }

        txtOutput = new javax.swing.JTextArea();
        txtOutput.setFont(font);
        txtOutput.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    txtOutput.setFont(
                            new Font("Consolas", Font.PLAIN, txtOutput.getFont().getSize() - e.getWheelRotation()));
                }
            }
        });

        jScrollPane2 = new javax.swing.JScrollPane();
        jSplitPane2.setDividerLocation(500);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setPreferredSize(new java.awt.Dimension(700, 600));
        jSplitPane2.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent evt) {
                jSplitPane2KeyReleased(evt);
            }
        });

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

        jSplitPane2.setRightComponent(jScrollPane2);

        getContentPane().add(jSplitPane2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtPHPKeyReleased(KeyEvent evt) {//GEN-FIRST:event_txtTSPHPKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_G && ((evt.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
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
    }//GEN-LAST:event_txtTSPHPKeyReleased

    private void jSplitPane2KeyReleased(KeyEvent evt) {//GEN-FIRST:event_jSplitPane2KeyReleased
        txtPHPKeyReleased(evt);
    }//GEN-LAST:event_jSplitPane2KeyReleased

    private void formKeyReleased(KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        txtPHPKeyReleased(evt);
    }//GEN-LAST:event_formKeyReleased

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
                dateFormat.format(new Date()) + ": Parsing and Definition phase completed\n"
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
                "\n" + dateFormat.format(new Date()) + ": Type checking completed\n"
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
        txtOutput.append(dateFormat.format(new Date()) + ": " + exception.getMessage() + "\n");
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