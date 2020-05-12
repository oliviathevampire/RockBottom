package de.ellpeck.rockbottom.launcher;

import de.ellpeck.rockbottom.Main;

import javax.swing.*;
import java.awt.*;

public class Launcher {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel textLabel = new JLabel("I'm a label in the window",SwingConstants.CENTER);
        textLabel.setPreferredSize(new Dimension(300, 100));
        frame.getContentPane().add(textLabel, BorderLayout.NORTH);

        JButton launchGameButton = new JButton("Play");
        launchGameButton.setBounds((1280-300)/2, 700-100, 300, 70);
        launchGameButton.setFont(launchGameButton.getFont().deriveFont(30.0F));
        launchGameButton.addActionListener(e -> {
            frame.setVisible(false);
            Main.main(args);
        });
        frame.getContentPane().add(launchGameButton, BorderLayout.CENTER);

        frame.setSize(1280,720);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
