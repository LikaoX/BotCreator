package com.sddev.botmaker;

import javax.print.Doc;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

public class PythonTextPane extends DarkJTextPane {


    public PythonTextPane() {
        super();
        getDocument().addDocumentListener(new PythonDocumentListener());
        ((AbstractDocument) getDocument()).setDocumentFilter(new PythonDocumentFilter());
        setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    private class PythonDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            if(!locked) {
                SwingUtilities.invokeLater(() -> PythonTextPane.this.insertUpdate(e));
            }
            SwingUtilities.invokeLater(() -> updateStyle());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(() -> updateStyle());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }
    }

    private class PythonDocumentFilter extends DocumentFilter {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            String text = getText();
            if(text.length() > 0) {
                char c = text.charAt(offset);
                char prev = offset - 1 >= 0 ? text.charAt(offset - 1) : '\0';
                char next = offset + 1 < text.length() ? text.charAt(offset + 1) : '\0';
                if(singleCharRemoved(offset, prev, c, next))
                    super.remove(fb, offset, length);
            } else super.remove(fb, offset, length);
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String replacement, AttributeSet attrs) throws BadLocationException {
            String text = getText(offset, length);
            if(!locked && length > 0 && text.contains("\n") && replacement.equals("\t")) {
                text = text.replaceAll("\n", "\n    ");
                text = "    " + text;
                lock();
                super.replace(fb, offset, length, text, attrs);
                unlock();
            } else super.replace(fb, offset, length, replacement, attrs);
        }
    }

    boolean locked2;
    private boolean singleCharRemoved(int offset, char prev, char c, char next) {
        Document document = getDocument();
        try {
            //locked2 = true;
            if(c == ')' && prev == '(') {
                document.remove(offset - 1, 2);
                return false;
            } else if(c == ']' && prev == '[') {
                document.remove(offset - 1, 2);
                return false;
            } else if(c == '}' && prev == '{') {
                document.remove(offset - 1, 2);
                return false;
            } else if(c == '\"' && prev == '\"') {
                document.remove(offset - 1, 2);
                return false;
            }
            if(!locked2) {
                locked2 = true;
                try {
                    if(c == '(' && next == ')') {
                        document.remove(offset, 2);
                        return false;
                    } else if(c == '[' && next == ']') {
                        document.remove(offset, 2);
                        return false;
                    } else if(c == '{' && next == '}') {
                        document.remove(offset, 2);
                        return false;
                    } else if(c == '\"' && next == '\"') {
                        document.remove(offset, 2);
                        return false;
                    }
                } finally {
                    locked2 = false;
                }
            }
        } catch(BadLocationException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    private void insertUpdate(DocumentEvent e) {
        String text = getText();
        if(e.getLength() == 1) {
            int i = e.getOffset();
            char c = text.charAt(i);
            char prev = i - 1 >= 0 ? text.charAt(i -1) : '\0';
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            singleCharInsert(i, prev, c, next);
        } else {
            try {
                String text2 = getText(e.getOffset(), e.getLength());
                lock();
                getDocument().remove(e.getOffset(), e.getLength());
                paste(e.getOffset(), text2);
                unlock();
            } catch(BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
    private boolean locked;
    private void singleCharInsert(int offset, char prev, char c, char next) {
        lock();
        Document document = getDocument();
        try {
            if(insertPairIfPossible(offset, c, next, '(', ')')) ;
            else if(insertPairIfPossible(offset, c, next, '[', ']')) ;
            else if(insertPairIfPossible(offset, c, next, '{', '}')) ;
            else if(insertPairIfPossible(offset, c, next, '\"', '\"')) ;
            else if(c == '\t') {
                document.remove(offset, 1);
                document.insertString(offset, "    ", null);
                setCaretPosition(offset + 4);
            } else if(c == '\n') {
                int spaces = getLeftSpacesInLineByOffset(offset);
                if(getLineByOffset(offset).trim().endsWith(":")) spaces += 4;
                getDocument().insertString(offset + 1, multiplySentence(" ", spaces), null);
                setCaretPosition(offset + 1 + spaces);
            } else if(prev == '(' && c == ')' && next == ')') {
                document.remove(offset + 1, 1);
            } else if(prev == '[' && c == ']' && next == ']') {
                document.remove(offset + 1, 1);
            } else if(prev == '{' && c == '}' && next == '}') {
                document.remove(offset + 1, 1);
            }
        } catch(BadLocationException ex) {
            ex.printStackTrace();
        }
        unlock();
    }

    public void insertStringAtCaret(String string) {
        lock();
        try {
            paste(getCaretPosition(), string);
        } catch(BadLocationException ex) {
            ex.printStackTrace();
        }
        unlock();
    }

    private void paste(int offset, String text) throws BadLocationException {
        text = text.replaceAll("\n", "\n" + multiplySentence(" ", getLeftSpacesInLineByOffset(offset)));
        getDocument().insertString(offset, text, null);
    }

    private int getLeftSpacesInLineByOffset(int offset) {
        String text = getLineByOffset(offset);
        int spaces = 0;
        for(int i = 0 ; i < text.length() && text.charAt(i) == ' ' ; i++)
            spaces++;
        return spaces;
    }

    private String getLineByOffset(int offset) {
        String text = getText();
        int l = 0;
        for(int i = 0 ; i < offset ; i++) {
            char c = text.charAt(i);
            if(c == '\n')
                l = i + 1;
        }
        int e = text.length() > l + 1 ? text.indexOf('\n', l + 1) : -1;
        return text.substring(l, e == -1 ? text.length() : e);
    }

    private int getLineNumberByOffset(int offset) {
        String text = getText();
        int line = 0;
        for(int i = 0 ; i < offset ; i++) {
            char c = text.charAt(i);
            if(c == '\n') line++;
        }
        return line;
    }

    private String multiplySentence(String sentence, int i) {
        StringJoiner joiner = new StringJoiner("");
        for(int j = 0 ; j < i ; j++)
            joiner.add(sentence);
        return joiner.toString();
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        SwingUtilities.invokeLater(() -> locked = false);
    }

    private boolean insertPairIfPossible(int offset, char c, char n, char l, char r) {
        if(c == l && (n != r && ignoredCharacters.contains(n))) {
            try {
                getDocument().insertString(offset + 1, String.valueOf(r), null);
                setCaretPosition(offset + 1);
                return true;
            } catch(BadLocationException ex) {

            }
        }
        return false;
    }


    private boolean[] updated;
    private void updateStyle() {
        String text = getText();
        updated = new boolean[text.length()];
        for(int i = text.length() - 1, j = -1 ; i >= 0 ; i--) {
            char c = text.charAt(i);
            if(j == -1) {
                if(c == '(') {
                    j = i - 1;
                }
            } else {
                boolean b = ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_');
                if(!b || i == 0) {
                    if(i == 0) i--;
                    setStyle(i + 1, j - i, functionStyle);
                    j = -1;
                }
            }
        }
        for(String keyword : keywords.keySet()) {
            int i = text.indexOf(keyword);
            while(i != -1) {
                keywordFound(text, keyword, i);
                i = text.indexOf(keyword, i + 1);
            }
        }
        for(int i = 0 ; i < text.length() ; i++) {
            char c = text.charAt(i);
            if(c >= '0' && c <= '9' && (i == 0 || ignoredCharacters.contains(text.charAt(i - 1)))) {
                int j = i;
                while(i < text.length() && (c = text.charAt(i)) >= '0' && c <= '9') i++;
                if(i == text.length() || ignoredCharacters.contains(text.charAt(i))) {
                    setStyle(j, i - j, numberStyle);
                }
            }
        }
        for(int i = 0 ; i < text.length() ; i++) {
            char c = text.charAt(i);
            if(c == '.' && isBetweenNumbers(text, i)) {
                setStyle(i, 1, numberStyle);
            }
        }
        for(int i = 0, j = -1 ; i < text.length() ; i++) {
            char c = text.charAt(i);
            if(c == '\"' && !isEscaped(text, i)) {
                if(j == -1) {
                    j = i;
                } else {
                    setStyle(j, i - j + 1, stringStyle);
                    j = -1;
                }
            }
        }
        for(int i = 0, j = -1 ; i < text.length() ; i++) {
            char c = text.charAt(i);
            if(c == '\'' && !isEscaped(text, i)) {
                if(j == -1) {
                    j = i;
                } else {
                    setStyle(j, i - j + 1, stringStyle2);
                    j = -1;
                }
            }
        }
        boolean commented = false;
        for(int i = 0 ; i < text.length() ; i++) {
            char c = text.charAt(i);
            char next = (char) (i + 1 < text.length() ? text.charAt(i + 1) : -1);
            if(c == '\\' && !isEscaped(text, i) && ignorableCharacters.contains(next)) {
                setStyle(i, 2, skipStyle);
            }
            if(c == '\n')
                commented = false;
            else if(c == '#')
                commented = true;

            if(commented)
                setStyle(i, 1, commentStyle);
        }
        resetStyle();
    }

    private void keywordFound(String text, String keyword, int offset) {
        boolean left = offset == 0 || ignoredCharacters.contains(text.charAt(offset - 1));
        boolean right = offset + keyword.length() >= text.length() || ignoredCharacters.contains(text.charAt(offset + keyword.length()));
        if(left && right) {
            setStyle(offset, keyword.length(), keywords.get(keyword));
        }
    }

    private void resetStyle() {
        for(int i = 0 ; i < updated.length ; i++) {
            if(!updated[i]) {
                int l = 0;
                while(i + l < updated.length && !updated[i + l]) l++;
                setStyle(i, l, defaultStyle);
            }
        }
    }

    private void setStyle(int offset, int length, Style style) {
        getStyledDocument().setCharacterAttributes(offset, length, style, false);
        for(int i = 0 ; i < length ; i++) {
            updated[offset + i] = true;
        }
    }

    private boolean isEscaped(String s, int i) {
        return i - 1 >= 0 && s.charAt(i - 1) == '\\' ? ! isEscaped(s, i -1) : false;
    }

    private boolean isBetweenNumbers(String text, int i) {
        boolean lb = false, rb = false;
        for(int j = i ; j >= 0 ; j--) {
            char c = text.charAt(j);
            if(c == ' ' || (c == '.' && j == i)) {
                continue;
            } else if(c >= '0' && c <= '9') {
                lb = true;
            } else break;
        }
        for(int j = i ; j < text.length() ; j++) {
            char c = text.charAt(j);
            if(c == ' ' || (c == '.' && j == i)) {
                continue;
            } else if(c >= '0' && c <= '9') {
                rb = true;
            } else break;
        }
        return lb && rb;
    }

    private static StyleContext styleContext = new StyleContext();
    private static HashMap<String, Style> keywords = new HashMap<>();
    private static ArrayList<Character> ignoredCharacters = new ArrayList<>(), ignorableCharacters = new ArrayList<>();
    private static Style defaultStyle, numberStyle, stringStyle, stringStyle2, skipStyle, commentStyle, functionStyle;
    static {
        defaultStyle = styleContext.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, Color.lightGray);
        StyleConstants.setBold(defaultStyle, false);
        numberStyle = styleContext.addStyle("number", null);
        StyleConstants.setForeground(numberStyle, Color.decode("#6897BB"));
        StyleConstants.setBold(numberStyle, false);
        stringStyle = styleContext.addStyle("string", null);
        StyleConstants.setForeground(stringStyle, Color.decode("#008080"));
        StyleConstants.setItalic(stringStyle, true);
        stringStyle2 = styleContext.addStyle("string2", null);
        StyleConstants.setForeground(stringStyle2, Color.decode("#A5C261"));
        StyleConstants.setItalic(stringStyle2, true);
        skipStyle = styleContext.addStyle("skip", null);
        StyleConstants.setForeground(skipStyle, Color.decode("#CC7832"));
        commentStyle = styleContext.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, Color.decode("#808080"));
        functionStyle = styleContext.addStyle("function", null);
        StyleConstants.setForeground(functionStyle, Color.decode("#8888C6"));

        ignoredCharacters.add('.');
        ignoredCharacters.add(',');
        ignoredCharacters.add('[');
        ignoredCharacters.add(']');
        ignoredCharacters.add('{');
        ignoredCharacters.add('}');
        ignoredCharacters.add('(');
        ignoredCharacters.add(')');
        ignoredCharacters.add(' ');
        ignoredCharacters.add('\n');
        ignoredCharacters.add(':');
        ignoredCharacters.add('\0');

        ignorableCharacters.add('\\');
        ignorableCharacters.add('n');
        ignorableCharacters.add('t');
        ignorableCharacters.add('t');
        ignorableCharacters.add('\"');
        ignorableCharacters.add('\'');
        ignorableCharacters.add('0');

        addKeyword("self", "94558D");
        addKeyword(",", "CC7832");
        addKeyword("decorator", "BBB529");
        addKeyword("class", "CC7832", true);
        addKeyword("def", "CC7832", true);
        addKeyword("import", "CC7832", true);
        addKeyword("from", "CC7832", true);
        addKeyword("print", "CC7832", true);
        addKeyword("__init__", "B200B2", true);
        addKeyword("__doc__", "B200B2", true);
        addKeyword("False", "CC7832", true);
        addKeyword("finally", "CC7832", true);
        addKeyword("is", "CC7832", true);
        addKeyword("return", "CC7832", true);
        addKeyword("None", "CC7832", true);
        addKeyword("continue", "CC7832", true);
        addKeyword("for", "CC7832", true);
        addKeyword("lambda", "CC7832", true);
        addKeyword("try", "CC7832", true);
        addKeyword("True", "CC7832", true);
        addKeyword("global", "CC7832", true);
        addKeyword("not", "CC7832", true);
        addKeyword("with", "CC7832", true);
        addKeyword("as", "CC7832", true);
        addKeyword("elif", "CC7832", true);
        addKeyword("if", "CC7832", true);
        addKeyword("or", "CC7832", true);
        addKeyword("yield", "CC7832", true);
        addKeyword("assert", "CC7832", true);
        addKeyword("else", "CC7832", true);
        addKeyword("import", "CC7832", true);
        addKeyword("pass", "CC7832", true);
        addKeyword("break", "CC7832", true);
        addKeyword("except", "CC7832", true);
        addKeyword("in", "CC7832", true);
        addKeyword("raise", "CC7832", true);
        addKeyword("while", "CC7832", true);
    }

    private static void addKeyword(String keyword, String color) {
        addKeyword(keyword, color, false);
    }

    private static void addKeyword(String keyword, String color, boolean isBold) {
        Style style = styleContext.addStyle(keyword, null);
        StyleConstants.setForeground(style, Color.decode((color.startsWith("#") ? "" : "#") + color));
        StyleConstants.setBold(style, isBold);
        keywords.put(keyword, style);
    }
}
