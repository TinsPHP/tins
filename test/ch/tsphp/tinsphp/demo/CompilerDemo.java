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
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import ch.tsphp.tinsphp.translators.tsphp.config.HardCodedTSPHPTranslatorInitialiser;
import org.antlr.runtime.RecognitionException;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class CompilerDemo extends JFrame implements ICompilerListener, IIssueLogger
{

    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    private final ICompiler compiler;


    private javax.swing.JTextArea txtOutput;
    private RSyntaxTextArea txtPhp;
    private RSyntaxTextArea txtTsphp;
    private RSyntaxTextArea txtPhpPlus;
    private JDialog helpDialog;


    /**
     * Creates new form CompilerDemo
     */
    public CompilerDemo() {
        initComponents();
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setIconImage(new ImageIcon(getClass().getResource("tinsphp.png")).getImage());

        compiler = new DemoTinsInitialiser().getCompiler();
        compiler.registerCompilerListener(this);
        compiler.registerIssueLogger(this);
    }

    /**
     * This method is called from within the constructor to initialise the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/tsphp", "ch.tsphp.tinsphp.demo.TSPHPTokenMaker");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TinsPHP Demonstration");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        initMenuBar();

        JSplitPane jSplitPane2 = new JSplitPane();
        JSplitPane jSplitPane1 = new JSplitPane();

        txtPhp = new RSyntaxTextArea(20, 60);
        txtPhp.setSyntaxEditingStyle("text/tsphp");
        txtPhp.setText("<?php\n\n?>");
        txtPhp.setCaretPosition(6);
        txtPhp.setCodeFoldingEnabled(false);

        RTextScrollPane scrollPHP = new RTextScrollPane(txtPhp);
        scrollPHP.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int size = validateSize(txtPhp.getFont().getSize() - e.getWheelRotation());
                    txtPhp.setFont(new Font("Consolas", Font.PLAIN, size));
                }
            }
        });

        txtTsphp = new RSyntaxTextArea(20, 60);
        txtTsphp.setSyntaxEditingStyle("text/tsphp");
        txtTsphp.setCodeFoldingEnabled(false);
        RTextScrollPane scrollTSPHP = new RTextScrollPane(txtTsphp);
        scrollTSPHP.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int size = validateSize(txtTsphp.getFont().getSize() - e.getWheelRotation());
                    txtTsphp.setFont(new Font("Consolas", Font.PLAIN, size));
                }
            }
        });

        txtPhpPlus = new RSyntaxTextArea(20, 60);
        txtPhpPlus.setSyntaxEditingStyle("text/tsphp");
        txtPhpPlus.setCodeFoldingEnabled(false);
        RTextScrollPane scrollMeta = new RTextScrollPane(txtPhpPlus);
        scrollMeta.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int size = validateSize(txtPhpPlus.getFont().getSize() - e.getWheelRotation());
                    txtPhpPlus.setFont(new Font("Consolas", Font.PLAIN, size));
                }
            }
        });


        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("tinsphp.xml"));
            theme.apply(txtPhp);
            theme = Theme.load(getClass().getResourceAsStream("tinsphp.xml"));
            theme.apply(txtTsphp);
            theme = Theme.load(getClass().getResourceAsStream("tinsphp.xml"));
            theme.apply(txtPhpPlus);
        } catch (Exception ex) {
            //well then, no custom theme for us
        }
        Font font = new Font("Consolas", Font.PLAIN, 18);
        txtPhp.setFont(font);
        txtTsphp.setFont(font);
        txtPhpPlus.setFont(font);

        txtOutput = new javax.swing.JTextArea();
        txtOutput.setFont(font);

        JScrollPane jScrollPane2 = new JScrollPane();
        jSplitPane2.setDividerLocation(500);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setPreferredSize(new java.awt.Dimension(700, 600));

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(339, 500));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(" TSPHP  ", null, scrollTSPHP, "TSPHP code (ALT + 1)");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab(" PHP+  ", null, scrollMeta, "PHP code including inferred types (ALT + 2)");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        jSplitPane1.setRightComponent(tabbedPane);
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
    }

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
        miTranslate.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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

        JMenu mHelp = new JMenu("Help");
        mHelp.setMnemonic(KeyEvent.VK_H);
        JMenuItem miHelp = new JMenuItem("Help");
        miHelp.setMnemonic(KeyEvent.VK_H);
        miHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        miHelp.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (helpDialog == null) {
                    helpDialog = new HelpDialog();
                }
                helpDialog.setVisible(true);
            }
        });
        mHelp.add(miHelp);

        JMenuItem miAbout = new JMenuItem("About");
        miAbout.setMnemonic(KeyEvent.VK_A);
        miAbout.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog aboutDialog = new AboutDialog();
                aboutDialog.setVisible(true);
            }
        });
        mHelp.add(miAbout);

        menuBar.add(mHelp);
        setJMenuBar(menuBar);
    }

    private void translate() {
        if (!compiler.isCompiling()) {
            String php = txtPhp.getText();
            if (!php.isEmpty()) {
                compiler.reset();
                txtOutput.setText("");
                txtTsphp.setText("");
                txtPhpPlus.setText("");
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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run() {
                new CompilerDemo().setVisible(true);
            }
        });
    }

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
        String tsphpOutput = translations.get(HardCodedTSPHPTranslatorInitialiser.class.getCanonicalName() + "_demo");
        String phpPlusOutput = translations.get(PhpPlusTranslatorInitialiser.class.getCanonicalName() + "_demo");
        txtTsphp.setText(tsphpOutput);
        txtPhpPlus.setText(phpPlusOutput);

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
