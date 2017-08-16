package com.sddev.botmaker;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.nio.file.Path;

public class Tab {
    private JTextPane textPane;
    private UndoManager undoManager;
    private Path path;
    private int number;

    public Tab(JTextPane textPane, UndoManager undoManager, Path path, int number) {
        this.textPane = textPane;
        this.undoManager = undoManager;
        this.path = path;
        this.number = number;
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public void setTextPane(JTextPane textPane) {
        this.textPane = textPane;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
