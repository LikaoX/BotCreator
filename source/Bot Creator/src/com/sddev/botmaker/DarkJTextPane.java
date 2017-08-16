package com.sddev.botmaker;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class DarkJTextPane extends JTextPane {
    {
        setBackground(new Color(49, 51, 53));
        setForeground(Color.LIGHT_GRAY);
        setCaretColor(Color.LIGHT_GRAY);
    }

    public DarkJTextPane() {
        super();
    }

    public DarkJTextPane(StyledDocument doc) {
        super(doc);
    }
}
