package com.vocabmaster;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {
    public AboutDialog(JFrame parent) {
        super(parent, "About VocabMaster", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("VocabMaster");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("Version 1.0");
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><center>A cross-platform vocabulary<br>learning tool.</center></html>");
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(version);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(desc);

        add(panel, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
