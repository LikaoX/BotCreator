package com.sddev.botmaker;

import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JConsoleOutput extends DarkJTextPane {

    private StyledDocumentPrintStream out, err;
    private StyledDocument document;

    public JConsoleOutput() {
        setEditable(false);
        document = getStyledDocument();
        StyleContext styleContext = new StyleContext();
        Style gray = styleContext.addStyle("gray", null);
        Style red = styleContext.addStyle("red", null);
        StyleConstants.setForeground(gray, Color.LIGHT_GRAY);
        StyleConstants.setForeground(red, new Color(228, 121, 115));
        try {
            Field field = System.class.getField("out");
            out = replaceStream(field, gray, System.out);
        } catch(NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        try {
            Field field = System.class.getField("err");
            err = replaceStream(field, red, System.err);
        } catch(NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }

    private StyledDocumentPrintStream replaceStream(Field field, Style style, PrintStream stream) {
        try {
            field.setAccessible(true);
            StyledDocumentPrintStream printStream = new StyledDocumentPrintStream(document, style, stream);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, printStream);
            return printStream;
        } catch(NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch(IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void setLogging(boolean logging) {
        out.setEnabled(logging);
        err.setEnabled(logging);
    }
}
