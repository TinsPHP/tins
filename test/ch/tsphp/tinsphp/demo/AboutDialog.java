/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.demo;


import javax.swing.*;
import java.awt.*;


class AboutDialog extends JDialog
{
    public AboutDialog() {
        setTitle("About");
        Container contentPane = getContentPane();
        setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        setIconImage(new ImageIcon(getClass().getResource("tinsphp.png")).getImage());
        contentPane.setBackground(Color.white);

//        add(Box.createRigidArea(new Dimension(0, 10)));


        JLabel lblText = new JLabel("<html><div style=\"margin:10px;font-size:12px;\">"
//                +"<p style=\"font-size:14px;font-weight:bold\">TinsPHP: Type Inference System for PHP</p>"
                + "<h2>TinsPHP: Type Inference System for PHP</h2>"
                + "<p>&copy; Copyright 2015 Robert Stoll <rstoll@tutteli.ch><br/><br/>"

                + "Licensed under the Apache License, Version 2.0 (the \"License\");<br/>"
                + "you may not use this file except in compliance with the License.<br/>"
                + "You may obtain a copy of the License at<br/><br/>"

                + "http://www.apache.org/licenses/LICENSE-2.0<br/><br/>"

                + "Unless required by applicable law or agreed to in writing, software<br/>"
                + "distributed under the License is distributed on an \"AS IS\" BASIS,<br/>"
                + "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,<br/>"
                + "either express or implied. See the License for the specific language<br/>"
                + "governing permissions and limitations under the License.</p>"
                + "</div></html>");
        lblText.setAlignmentX(0.5f);
        lblText.setPreferredSize(new Dimension(510, 350));
        add(lblText);

//        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }
}
