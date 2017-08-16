package com.sddev.botmaker;

import bot.Bot;
import org.jnativehook.GlobalScreen;
import org.python.core.*;
import org.python.util.InteractiveInterpreter;
import org.python.util.PythonInterpreter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;

public class MainWindow extends JFrame {
    private JPanel mainPanel;
    private JTree filesTree;
    private JTabbedPane tabbedPane;
    private GlobalListener globalListener;
    private Path projectDirectory;
    private ArrayList<Tab> tabs;
    private ScreenshotMaker screenshotMaker;
    private JConsoleOutput logsTextPane;
    private JScrollPane logsScrollPane;
    private JLabel statusBar;
    private JScrollPane scroll;
    private ProjectsList list;
    private MouseInfoDialog mouseInfoDialog;
    private KeyAdapter keyAdapter = new KeyAdapter() {
        private ArrayList<Integer> keysPressed = new ArrayList<>();
        @Override
        public void keyPressed(KeyEvent e) {
            if(!keysPressed.contains(e.getKeyCode()))
                keysPressed.add(e.getKeyCode());
            if(keysPressed.size() == 2 && keysPressed.get(0) == KeyEvent.VK_CONTROL && keysPressed.get(1) == KeyEvent.VK_S) {
                saveAll();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if(keysPressed.contains(e.getKeyCode()))
                keysPressed.remove(keysPressed.indexOf(e.getKeyCode()));
        }
    };

    @Override
    public void dispose() {
        GlobalScreen.removeNativeKeyListener(globalListener);
        GlobalScreen.removeNativeMouseListener(globalListener);
        GlobalScreen.removeNativeMouseMotionListener(globalListener);
        mouseInfoDialog.dispose();
        super.dispose();
    }

    public MainWindow(Path projectDirectory, ProjectsList list) {
        this.list = list;
        setTitle(Strings.get("applicationTitle"));
        UIManager.put("OptionPane.yesButtonText", Strings.get("yes"));
        UIManager.put("OptionPane.noButtonText", Strings.get("no"));
        UIManager.put("OptionPane.cancelButtonText", Strings.get("cancel"));
        this.projectDirectory = projectDirectory;
        Bot.projectDirectory = projectDirectory;
        this.screenshotMaker = new ScreenshotMaker();
        if(!Files.exists(projectDirectory) || !Files.isDirectory(projectDirectory)) {
            try {
                Files.deleteIfExists(projectDirectory);
                Files.createDirectories(projectDirectory);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        try {
            setIconImage(ImageIO.read(getClass().getResource("resources/robot.png")));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        globalListener = new GlobalListener() {
            @Override
            public void functionKeyPressed(int keyCode) {
                MainWindow.this.functionKeyPressed(keyCode);
            }
        };
        GlobalScreen.addNativeKeyListener(globalListener);
        GlobalScreen.addNativeMouseListener(globalListener);
        GlobalScreen.addNativeMouseMotionListener(globalListener);
        setMinimumSize(mainPanel.getMinimumSize());
        setContentPane(mainPanel);
        setLocation(100, 100);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setJMenuBar(new MainJMenuBar());
        updateFilesTree();
        tabs = new ArrayList<>();
        filesTree.setCellRenderer(new CellTreeRenderer());
        filesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = filesTree.getRowForLocation(e.getX(), e.getY());
                TreePath treePath = filesTree.getPathForLocation(e.getX(), e.getY());
                if(row != -1) {
                    Path path = ((PathNode) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject()).getPath();
                    if(e.getButton() == MouseEvent.BUTTON3) {
                        filesTree.setSelectionPath(treePath);
                        JTreeMenu jTreeMenu = new JTreeMenu(path);
                        jTreeMenu.show(filesTree, e.getX(), e.getY());
                    } else if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        String absolute = path.toAbsolutePath().toString();
                        if(getTabByPath(path) == null && absolute.endsWith(".py")) {
                            PythonTextPane textPane = new PythonTextPane();
                            textPane.addKeyListener(keyAdapter);
                            textPane.setFocusable(true);
                            UndoManager undoManager = new UndoManager();
                            textPane.getDocument().addUndoableEditListener(new UndoableEditListener() {
                                @Override
                                public void undoableEditHappened(UndoableEditEvent e) {
                                    undoManager.addEdit(e.getEdit());
                                }
                            });
                            try {
                                StringJoiner joiner = new StringJoiner("\n");
                                for(String line : Files.readAllLines(path)) {
                                    joiner.add(line);
                                }
                                textPane.setText(joiner.toString());
                                tabbedPane.setFocusable(false);
                                tabbedPane.addTab(path.getFileName().toString(), new JScrollPane(textPane));
                                Tab tab = new Tab(textPane, undoManager, path, tabbedPane.getTabCount() - 1);
                                tabs.add(tab);
                            } catch(IOException ex) {
                                JOptionPane.showMessageDialog(MainWindow.this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                            }
                        } else if(!Files.isDirectory(path)) {
                            try {
                                Desktop.getDesktop().open(path.toFile());
                            } catch(IOException ex) {
                                JOptionPane.showMessageDialog(MainWindow.this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final int index = tabbedPane.getUI().tabForCoordinate(tabbedPane,e .getX(), e.getY());
                if(index != -1) {
                    if(SwingUtilities.isRightMouseButton(e)) {
                        closeTab(tabs.get(index));
                    }
                }
            }
        });
        setFocusable(true);
        logsScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            BoundedRangeModel brm = logsScrollPane.getVerticalScrollBar().getModel();
            boolean wasAtBottom = true;

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!brm.getValueIsAdjusting()) {
                    if (wasAtBottom)
                        brm.setValue(brm.getMaximum());
                } else
                    wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());

            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        getRootPane().setFocusable(true);
        logsTextPane.setLogging(true);
        scroll.setViewportView(filesTree);
        pack();
        setVisible(true);
    }

    private Thread applicationThread;

    private void startApplication() {
        if(applicationThread == null && !globalListener.isRecording()) {
            logsTextPane.setLogging(true);
            saveAll();
            applicationThread = new ApplicationThread();
            applicationThread.start();
        }
    }

    private void stopApplication() {
        if(applicationThread != null && !applicationThread.isInterrupted()) {
            setStatus(Strings.get("script_terminated"), 2);
            currentStatusPriority = 0;
            logsTextPane.setLogging(false);
            try {
                applicationThread.stop(); //W następnej wersji funkcja zostanie zastąpiona
            } catch(ThreadDeath death) {

            }
            applicationThread = null;
        }
    }

    private class ApplicationThread extends Thread {
        public ApplicationThread() {

        }
        private InteractiveInterpreter interpreter;
        @Override
        public void run() {
            setStatus(Strings.get("script_started"), 2);
            try {
                Py.setSystemState(new PySystemState());
                Py.getThreadState().tracefunc = new TraceFunction() {
                    @Override
                    public TraceFunction traceCall(PyFrame frame) {
                        return null;
                    }

                    @Override
                    public TraceFunction traceReturn(PyFrame frame, PyObject ret) {
                        return null;
                    }

                    @Override
                    public TraceFunction traceLine(PyFrame frame, int line) {
                        return null;
                    }

                    @Override
                    public TraceFunction traceException(PyFrame frame, PyException exc) {
                        return null;
                    }
                };
                interpreter = new InteractiveInterpreter();
                interpreter.getSystemState().path.append(new PyString(projectDirectory.toAbsolutePath().toString()));
                interpreter.execfile(Files.newInputStream(Paths.get(projectDirectory.toAbsolutePath().toString(), "main.py")),"main");
                setStatus(Strings.get("script_finished"), 2);
            } catch(Exception ex) {
                if(!Thread.interrupted()) {
                    System.out.println("\nProcess error: ");
                    setStatus(Strings.get("script_error"), 2);
                    ex.printStackTrace();
                } else {
                    setStatus(Strings.get("script_finished"), 2);
                }
            } finally {
                logsTextPane.setLogging(false);
                applicationThread = null;
                currentStatusPriority = 0;
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
        }
    }

    private int currentStatusPriority = -1;
    private void setStatus(String status) {
        setStatus(status, 0);
    }
    private void setStatus(String status, int priority) {
        if(priority >= currentStatusPriority) {
            statusBar.setText(status + " " + getCurrentTimeStamp());
            currentStatusPriority = priority;
        }
    }

    private String getCurrentTimeStamp() {
        return new SimpleDateFormat(Strings.get("status_bar_time_stamp_format")).format(new Date());
    }

    private void updateFilesTree() {
        MutableTreeNode root = TreeUtils.listFilesAsNode(projectDirectory);
        TreeModel treeModel = new DefaultTreeModel(root);
        filesTree.setModel(treeModel);
    }

    private void functionKeyPressed(int key) {
        switch(key) {
            case 3:
                startRecording();
                break;
            case 4:
                new Thread(() -> stopRecording()).start();
                break;
            case 6:
                makeScreenshot();
                break;
            case 7:
                startApplication();
                break;
            case 8:
                stopApplication();
                break;
        }
    }

    private void saveTab(int number) {
        Tab tab = getTab(number);
        if(tab != null) {
            try {
                Files.write(tab.getPath(), tab.getTextPane().getText().getBytes());
                setStatus(Strings.get("file_saved").replaceAll("%FILE%", (projectDirectory.relativize(tab.getPath())).toString()));
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Tab getTab(int number) {
        Tab tab = null;
        for(Tab t : tabs) {
            if(t.getNumber() == number) {
                tab = t;
                break;
            }
        }
        return tab;
    }

    private void saveAll() {
        for(int i = 0 ; i < tabbedPane.getTabCount() ; i++)
            saveTab(i);
        setStatus(Strings.get("all_files_saved"));
    }

    private Tab getTabByPath(Path path) {
        Tab tab = null;
        for(Tab t : tabs) {
            try {
                if(Files.isSameFile(t.getPath(), path)) {
                    tab = t;
                    break;
                }
            } catch(IOException ex) {
            }
        }
        return tab;
    }

    private void startRecording() {
        if(!globalListener.isRecording() && applicationThread == null) {
            globalListener.startRecording();
            setStatus(Strings.get("recording_started"), 2);
        }
    }

    private void stopRecording() {
        if(globalListener.isRecording()) {
            setStatus(Strings.get("recording_stopped"), 2);
            currentStatusPriority = 0;
            Record record = globalListener.stopRecording();
            Tab tab = getTab(tabbedPane.getSelectedIndex());
            if(tab != null) {
                PythonTextPane pythonTextPane = (PythonTextPane) tab.getTextPane();
                pythonTextPane.insertStringAtCaret(record.generatePythonCode());
            }
        }
    }

    private void makeScreenshot() {
        BufferedImage screenshot = screenshotMaker.makeScreenshot();
        if(screenshot != null) {
            String name = JOptionPane.showInputDialog(this, Strings.get("file_name_request"), Strings.get("new_screenshot"), JOptionPane.QUESTION_MESSAGE);
            if(name != null) {
                String extension = "png";
                if(name.contains(".") && !name.endsWith(".")) {
                    extension = name.substring(name.indexOf(".") + 1);
                } else {
                    name += ".png";
                }
                try {
                    Path path = projectDirectory.resolve(name);
                    setStatus(Strings.get("screenshot_saved").replaceAll("%FILE%", projectDirectory.relativize(path).toString()));
                    ImageIO.write(screenshot, extension, Files.newOutputStream(path));
                    updateFilesTree();
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exit() {
        int i = JOptionPane.showConfirmDialog(this, Strings.get("do_you_wanna_save"), Strings.get("exit"), JOptionPane.YES_NO_CANCEL_OPTION);
        switch(i) {
            case JOptionPane.YES_OPTION:
                saveAll();
                System.exit(0);
                break;
            case JOptionPane.NO_OPTION:

                System.exit(0);
                break;
        }
    }

    private class MainJMenuBar extends JMenuBar {
        private JMenu project, recording, bot, mouse, help;
        private JMenuItem newProject, loadProject, save, saveAll, exit;
        private JMenuItem startRecording, stopRecording, makeScreenshot;
        private JMenuItem startBot, stopBot;
        private JMenuItem showPointerInfo, hidePointerInfo;
        private JMenuItem documentation;


        public MainJMenuBar() {
            project = new JMenu(Strings.get("file"));

            newProject = new JMenuItem(Strings.get("new"));
            loadProject = new JMenuItem(Strings.get("open"));
            save = new JMenuItem(Strings.get("save"));
            saveAll = new JMenuItem(Strings.get("save_all") + " [Ctrl+S]");
            exit = new JMenuItem(Strings.get("exit2"));
            newProject.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.this.dispose();
                    new NewProjectWindow(list);
                }
            });
            loadProject.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.this.dispose();
                    new OpenProjectWindow(list);
                }
            });
            save.addActionListener((e) -> saveTab(tabbedPane.getSelectedIndex()));
            saveAll.addActionListener((e) -> saveAll());
            exit.addActionListener((e) -> exit());
            project.add(newProject);
            project.add(loadProject);
            project.add(save);
            project.add(saveAll);
            project.add(exit);

            recording = new JMenu(Strings.get("recording"));
            startRecording = new JMenuItem(Strings.get("record") + " [F3]");
            stopRecording = new JMenuItem(Strings.get("stop_recording") + " [F4]");
            makeScreenshot = new JMenuItem(Strings.get("make_screenshot") + " [F6]");
            startRecording.addActionListener((e) -> startRecording());
            stopRecording.addActionListener((e) -> stopRecording());
            makeScreenshot.addActionListener((e) -> makeScreenshot());
            recording.add(startRecording);
            recording.add(stopRecording);
            recording.add(makeScreenshot);

            bot = new JMenu(Strings.get("script"));
            startBot = new JMenuItem(Strings.get("run_script") + " [F7]");
            stopBot = new JMenuItem(Strings.get("stop_script") + " [F8]");
            startBot.addActionListener((e) -> startApplication());
            stopBot.addActionListener((e) -> stopApplication());
            bot.add(startBot);
            bot.add(stopBot);

            mouse = new JMenu(Strings.get("mouse_pointer"));
            showPointerInfo = new JMenuItem(Strings.get("show_mouse_pointer_info"));
            hidePointerInfo = new JMenuItem(Strings.get("hide_mouse_pointer_info"));
            showPointerInfo.addActionListener((e) -> showPointerInfo());
            hidePointerInfo.addActionListener((e) -> hidePointerInfo());
            mouse.add(showPointerInfo);
            mouse.add(hidePointerInfo);

            help = new JMenu(Strings.get("help"));
            documentation = new JMenuItem(Strings.get("documentation"));
            documentation.addActionListener((e) -> new DocumentationWindow(null));
            help.add(documentation);

            add(project);
            add(bot);
            add(recording);
            add(mouse);
            add(help);
        }
    }

    private void showPointerInfo() {
        if(mouseInfoDialog == null) {
            mouseInfoDialog = new MouseInfoDialog(globalListener.getPointerInfo());
        }
    }

    private void hidePointerInfo() {
        if(mouseInfoDialog != null) {
            mouseInfoDialog.dispose();
            mouseInfoDialog = null;
        }
    }

    private void closeTab(Tab tab) {
        if(tab != null) {
            tabs.remove(tab);
            tabbedPane.remove(tab.getNumber());
            for(Tab t : tabs) {
                if(t.getNumber() > tab.getNumber())
                    t.setNumber(t.getNumber() - 1);
            }
        }
    }

    private FileTransfer fileTransfer;

    enum FileTransferOperation {cut, copy}
    private class FileTransfer {
        private Path path;
        private FileTransferOperation operation;

        public FileTransfer(Path path, FileTransferOperation operation) {
            this.path = path;
            this.operation = operation;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public FileTransferOperation getOperation() {
            return operation;
        }

        public void setOperation(FileTransferOperation operation) {
            this.operation = operation;
        }
    }

    private class JTreeMenu extends JPopupMenu {
        private Path path;

        public JTreeMenu(Path path) {
            this.path = path;
            JMenuItem newFile = new JMenuItem("Nowy plik");
            JMenuItem newDirectory = new JMenuItem("Nowy folder");
            JMenuItem cutFile = new JMenuItem("Wytnij");
            JMenuItem copyFile = new JMenuItem("Kopiuj");
            JMenuItem pasteFile = new JMenuItem("Wklej");
            JMenuItem renameFile = new JMenuItem("Zmień nazwę");
            JMenuItem deleteFile = new JMenuItem("Usuń");
            JMenuItem closeTab = new JMenuItem("Zamknij kartę");
            newFile.addActionListener((e) -> newFile());
            newDirectory.addActionListener((e) -> newDirectory());
            cutFile.addActionListener((e) -> cutFile());
            copyFile.addActionListener((e) -> copyFile());
            pasteFile.addActionListener((e) -> pasteFile());
            renameFile.addActionListener((e) -> renameFile());
            deleteFile.addActionListener((e) -> deleteFile());
            closeTab.addActionListener((e) -> closeTab());
            pasteFile.setEnabled(fileTransfer != null);
            add(newFile);
            add(newDirectory);
            add(cutFile);
            add(copyFile);
            add(pasteFile);
            add(deleteFile);
            add(renameFile);

            Tab tab = null;
            for(Tab t : tabs) {
                try {
                    if(Files.isSameFile(t.getPath(), path)) {
                        tab = t;
                        break;
                    }
                } catch(IOException ex) {
                }
            }
            if(path.toString().endsWith(".py") && tab != null)
                add(closeTab);
        }

        private void renameFile() {
            String name = JOptionPane.showInputDialog(this, Strings.get("file_name_request"), Strings.get("name_change"), JOptionPane.QUESTION_MESSAGE);
            if(name != null) {
                try {
                    Files.move(path, Paths.get(path.getParent().toAbsolutePath().toString(), name));
                    closeTab();
                    updateFilesTree();
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void closeTab() {
            Tab tab = getTabByPath(path);
            if(tab != null) {
                tabs.remove(tab);
                tabbedPane.remove(tab.getNumber());
                for(Tab t : tabs) {
                    if(t.getNumber() > tab.getNumber())
                        t.setNumber(t.getNumber() - 1);
                }
            }
        }

        private void newFile() {
            String name = JOptionPane.showInputDialog(this, Strings.get("file_name_request"), Strings.get("new_file"), JOptionPane.QUESTION_MESSAGE);
            String parent;
            if(name != null) {
                if(Files.isDirectory(path))
                    parent = path.toAbsolutePath().toString();
                else
                    parent = path.getParent().toAbsolutePath().toString();
                Path path = Paths.get(parent, name);
                try {
                    Files.createFile(path);
                    updateFilesTree();
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void newDirectory() {
            String name = JOptionPane.showInputDialog(this, Strings.get("directory_name_request"), Strings.get("new_directory"), JOptionPane.QUESTION_MESSAGE);
            String parent;
            if(name != null) {
                if(Files.isDirectory(path))
                    parent = path.toAbsolutePath().toString();
                else
                    parent = path.getParent().toAbsolutePath().toString();
                Path path = Paths.get(parent, name);
                try {
                    Files.createDirectory(path);
                    updateFilesTree();
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void cutFile() {
            fileTransfer = new FileTransfer(path, FileTransferOperation.cut);
        }

        private void copyFile() {
            fileTransfer = new FileTransfer(path, FileTransferOperation.copy);
        }

        private void pasteFile() {
            String parent;
            Path p = fileTransfer.getPath();
            if(Files.isDirectory(path))
                parent = path.toAbsolutePath().toString();
            else
                parent = path.getParent().toAbsolutePath().toString();
            Path path = Paths.get(parent, p.getFileName().toString());
            try {
                if(fileTransfer != null) {
                    if(fileTransfer.operation == FileTransferOperation.cut) {
                        Files.move(p, path);
                    } else {
                        Files.copy(p, path);
                    }
                }
                updateFilesTree();
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteFile() {
            try {
                if(!Files.isSameFile(path, projectDirectory)) {
                    delete(path.toFile());
                    updateFilesTree();
                } else {
                    JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
                }
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(this, Strings.get("operation_fail"), Strings.get("error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        private void delete(File f) throws IOException {
            if (f.isDirectory()) {
                for (File c : f.listFiles())
                    delete(c);
            }
            if (!f.delete())
                throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
    private static class CellTreeRenderer extends DefaultTreeCellRenderer {
        private static Icon scriptIcon;
        private static Icon pictureIcon;
        private static Icon fileIcon;
        static {
            try {
                scriptIcon = new ImageIcon(ImageIO.read(MainWindow.class.getResource("resources/py.png")));
            } catch(IOException ex) {
                scriptIcon = UIManager.getIcon("Tree.leafIcon");
            }
            try {
                pictureIcon = new ImageIcon(ImageIO.read(MainWindow.class.getResource("resources/picture.png")));
            } catch(IOException ex) {
                pictureIcon = UIManager.getIcon("Tree.leafIcon");
            }
            try {
                fileIcon = new ImageIcon(ImageIO.read(MainWindow.class.getResource("resources/file.png")));
            } catch(IOException ex) {
                fileIcon = UIManager.getIcon("Tree.leafIcon");
            }
        }
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            PathNode pathNode = (PathNode) node.getUserObject();
            if(pathNode.isDirectory()) {
                setIcon(UIManager.getIcon(expanded ? "Tree.openIcon" : "Tree.closedIcon"));
            } else if(pathNode.isScript()) {
                setIcon(scriptIcon);
            } else if(pathNode.isImage()) {
                setIcon(pictureIcon);
            } else {
                setIcon(fileIcon);
            }
            return this;
        }
    }
}