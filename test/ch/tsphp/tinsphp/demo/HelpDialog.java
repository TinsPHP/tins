/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.demo;


import javax.swing.*;
import java.awt.*;


class HelpDialog extends JDialog
{
    public HelpDialog() {
        setTitle("Help");
        Container contentPane = getContentPane();
        setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        setIconImage(new ImageIcon(getClass().getResource("tinsphp.png")).getImage());
        contentPane.setBackground(Color.white);

//        add(Box.createRigidArea(new Dimension(0, 10)));

        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

        JLabel lblText = new JLabel("<html><div style=\"margin:10px;font-size:12px;margin-top:0;\">"
//                +"<div style=\"font-size:14px;font-weight:bold\">TinsPHP: Type Inference System for PHP</div>"
                + "<h2 style=\"margin-top:0px\">TinsPHP: Type Inference System for PHP</h2>"
                + "<p>This demo client application shows how PHP is translated to TSPHP and to an intermediary "
                + "from called PHP+ (PHP syntax enriched with the inferred types). PHP+ supports union types in "
                + "contrast to TSPHP and shows the full potential of the inference algorithm.<p>"
                + "<p>Use the menu \"Run -> Translate\" or press " + (isMac ? "Command + T" : "CTRL + T") + " to "
                + "start the translation. The translation performs the following phases "
                + "were they would usually be performed in parallel if multiple PHP files were translated:"
                + "<ol style=\"margin-left:20px\">"
                + "<li>Parse PHP code and create an abstract syntax tree (AST) out of it. "
                + "Walk the AST and gather definitions (function, class, interface definitions and more).</li>"
                + "<li>Walk the AST and resolve references (e.g. resolve the function definition of a particular "
                + "function call). Create constraints for all applications (function, method, operator and "
                + "flow-of-control statements).</li>"
                + "<li>Solve the constraints (type inference algorithm).</li>"
                + "<li>Translated the AST augmented with type information to the target languages "
                + "(TSPHP and PHP+).</li>"
                + "</ol>"
                + "</p>"
                + "<p>Visit the official website for more information about the TinsPHP project: http://tsphp" +
                ".ch/tins</p>"
                + "</div></html>");
        lblText.setAlignmentX(0.5f);
        lblText.setPreferredSize(new Dimension(545, 450));

        JScrollPane scrollPane = new JScrollPane(lblText);
        scrollPane.getViewport().setBackground(Color.white);
        scrollPane.setPreferredSize(new Dimension(565, 500));
        add(scrollPane);

//        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }
}
