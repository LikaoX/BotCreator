package com.sddev.botmaker;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class StyledDocumentPrintStream extends PrintStream {
    private StyledDocument styledDocument;
    private Style style;
    private boolean enabled;
    private PrintStream printStream;
    public StyledDocumentPrintStream(StyledDocument styledDocument, Style style, PrintStream printStream) {
        super(new NullOutputStream(), true);
        this.styledDocument = styledDocument;
        this.style = style;
        this.printStream = printStream;
    }

    @Override
    public void write(int b) {
        try {
            if(enabled)
                styledDocument.insertString(styledDocument.getLength(), String.valueOf((char) b), style);
            if(printStream != null)
                printStream.write(b);
        } catch(BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        try {
            if(enabled)
                styledDocument.insertString(styledDocument.getLength(), new String(buf, off, len), style);
            if(printStream != null)
                printStream.write(buf, off, len);
        } catch(BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            if(enabled)
                styledDocument.insertString(styledDocument.getLength(), new String(b), style);
            if(printStream != null)
                printStream.write(b);
        } catch(BadLocationException ex) {
            ex.printStackTrace();
        }
    }
    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {

        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
