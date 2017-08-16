package com.sddev.botmaker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.StringJoiner;

public class DocumentationWindow extends JDialog {

    private JPanel contentPane;
    private JTree tree1;
    private JScrollPane scroll;
    private JTextField headerLabel;
    private JTextArea descriptionLabel;

    public DocumentationWindow(JFrame parent) {
        super(parent);
        headerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    StringSelection selection = new StringSelection(headerLabel.getText());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                }
            }
        });
        setTitle(Strings.get("applicationTitle") + " - " + Strings.get("documentation"));
        setContentPane(contentPane);
        pack();
        try {
            setIconImage(ImageIO.read(getClass().getResource("resources/robot.png")));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        setLocation((displayMode.getWidth() - getWidth()) / 2, (displayMode.getHeight() - getHeight()) / 2);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(DocumentationWindow.class.getResourceAsStream("xml/documentation.xml"));
            DefaultMutableTreeNode root = loadDocumentation(document.getDocumentElement());
            DefaultTreeModel model = new DefaultTreeModel(root);
            tree1.setModel(model);
        } catch(ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch(SAXException ex) {
            ex.printStackTrace();
        }
        tree1.setCellRenderer(new DocumentationTreeCellRenderer());
        tree1.addTreeSelectionListener((e) -> treeItemSelected(e));
        scroll.setViewportView(tree1);
        setAlwaysOnTop(true);
        setVisible(true);
    }

    private DocumentationNode loadDocumentation(Element element) {
        DocumentationNode node = null;
        String tag = element.getTagName();
        String header = element.getAttribute("header");
        String description = element.getAttribute("description");
        String clazz = element.getAttribute("class");
        if(tag.equals("ClassNode")) {
            if(!clazz.isEmpty()) {
                try {
                    Class<?> c = Class.forName(clazz);
                    node = new ClassNode(c);
                    if(!header.isEmpty()) node.header = header;
                    if(!description.isEmpty()) node.description = description;
                } catch(ClassNotFoundException ex) {

                }
            }
        } else if(tag.equals("Node")) {
            node = new DocumentationNode(header, description);
        }
        if(node != null) {
            NodeList nodeList = element.getChildNodes();
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                if(n instanceof Element) {
                    node.add(loadDocumentation((Element) n));
                }
            }
        }
        return node == null ? new DocumentationNode("null", "null") : node;
    }

    private void treeItemSelected(TreeSelectionEvent e) {
        JTree tree = (JTree) e.getSource();
        Object object = tree.getLastSelectedPathComponent();
        if(object instanceof DocumentationNode) {
            DocumentationNode node = (DocumentationNode) object;
            headerLabel.setText(node.header);
            descriptionLabel.setText(node.description);
        } else {
            headerLabel.setText("");
            descriptionLabel.setText("");
        }
    }

    private static class FieldNode extends DocumentationNode {
        private Field field;
        public FieldNode(Field field) {
            super("", "");
            this.field = field;
            header = toString();
        }

        @Override
        public String toString() {
            String s = field.getType().getSimpleName() + " " + field.getName();
            if(Modifier.isStatic(field.getModifiers()))
                s = "static " + s;
            return s;
        }
    }

    private static class DocumentationNode extends DefaultMutableTreeNode {
        protected String header, description;

        public DocumentationNode(String header, String description) {
            this.header = header;
            this.description = description;
        }

        @Override
        public String toString() {
            return header;
        }
    }

    private static class ClassNode extends DocumentationNode {
        private Class<?> clazz;
        public ClassNode(Class<?> clazz) {
            super(clazz.getName(), clazz.getName());
            this.clazz = clazz;
            for(Field field : clazz.getFields()) {
                add(new FieldNode(field));
            }
            for(Constructor<?> constructor : clazz.getConstructors()) {
                boolean b = false;
                for(Constructor<?> c : Object.class.getConstructors()) {
                    if(c.equals(constructor)) {
                        b = true;
                        break;
                    }
                }
                if(b) continue;
                add(new ConstructorNode(constructor));
            }
            for(Method method : clazz.getMethods()) {
                boolean b = false;
                for(Method m : Object.class.getMethods()) {
                    if(m.equals(method)) {
                        b = true;
                        break;
                    }
                }
                if(b) continue;
                add(new MethodNode(method));
            }
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }

    private static class MethodNode extends DocumentationNode {
        private Method method;

        public MethodNode(Method method) {
            super(null, "");
            this.method = method;
            header = toString();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ");
            for(Class<?> argument : method.getParameterTypes()) {
                joiner.add(argument.getSimpleName());
            }
            String s = method.getReturnType().getSimpleName() + " " + method.getName() + "(" + joiner.toString() + ')';
            if(Modifier.isStatic(method.getModifiers()))
                s = "static " + s;
            return s;
        }
    }

    private static class ConstructorNode extends DocumentationNode {
        private Constructor<?> constructor;

        public ConstructorNode(Constructor<?> constructor) {
            super(null, "");
            this.constructor = constructor;
            header = toString();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ");
            for(Class<?> argument : constructor.getParameterTypes()) {
                joiner.add(argument.getSimpleName());
            }
            return constructor.getDeclaringClass().getSimpleName() + "(" + joiner.toString() + ')';
        }
    }

    private static class DocumentationTreeCellRenderer extends DefaultTreeCellRenderer {
        private static Icon classIcon, functionIcon, folderIcon, informationIcon;
        static {
            try {
                classIcon = new ImageIcon(ImageIO.read(DocumentationTreeCellRenderer.class.getResource("resources/class.png")));
            } catch(IOException ex) {
                classIcon = UIManager.getIcon("Tree.openIcon");
            }
            try {
                functionIcon = new ImageIcon(ImageIO.read(DocumentationTreeCellRenderer.class.getResource("resources/function.png")));
            } catch(IOException ex) {
                functionIcon = UIManager.getIcon("Tree.leafIcon");
            }
            try {
                folderIcon = new ImageIcon(ImageIO.read(DocumentationTreeCellRenderer.class.getResource("resources/folder.png")));
            } catch(IOException ex) {
                folderIcon = UIManager.getIcon("Tree.openIcon");
            }
            try {
                informationIcon = new ImageIcon(ImageIO.read(DocumentationTreeCellRenderer.class.getResource("resources/info.png")));
            } catch(IOException ex) {
                informationIcon = UIManager.getIcon("Tree.leafIcon");
            }
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            MutableTreeNode node = (MutableTreeNode) value;
            if(node instanceof MethodNode || node instanceof ConstructorNode) {
                setIcon(functionIcon);
            } else if(node instanceof ClassNode) {
                setIcon(classIcon);
            } else if(node.getChildCount() > 0) {
                setIcon(folderIcon);
            } else if(node instanceof FieldNode) {

            } else if(node instanceof DocumentationNode) {
                setIcon(informationIcon);
            }
            return this;
        }
    }
}
