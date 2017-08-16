package com.sddev.botmaker;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectsListCell {
    private JPanel cell;
    private JLabel text1;
    private JLabel text2;
    private Path path;
    public static final Color
            defaultBackground = new Color(255, 255, 255),
            hoverBackground = new Color(221, 221, 221),
            pressedBackground = new Color(203, 203, 203);

    public ProjectsListCell(String text1, Path path) {
        setText1(text1);
        this.path = path;
        setText2(path.toAbsolutePath().toString());
    }

    public JPanel getCell() {
        return cell;
    }

    public void setText1(String text) {
        text1.setText(text);
    }

    public void setText2(String text) {
        text2.setText(text);
    }

    public boolean runProject(ProjectsList list) {
        if(Files.exists(path) && Files.isDirectory(path)) {
            new Thread(() -> new MainWindow(path, list)).start();
            return true;
        } else return false;
    }
}
