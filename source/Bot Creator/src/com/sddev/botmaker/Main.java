package com.sddev.botmaker;


import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.python.util.PythonInterpreter;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
        } catch(NativeHookException ex) {
            ex.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        new PythonInterpreter();
        //SwingUtilities.invokeLater(() -> new ProjectsWindow());
        SwingUtilities.invokeLater(() -> new ProjectsWindow());
    }
}
