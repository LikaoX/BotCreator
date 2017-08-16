package com.sddev.botmaker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewProjectWindow extends JFrame {

    private ProjectsList list;
    private JPanel contentPane;
    private JTextField nameTextField;
    private JTextField pathTextField;
    private JButton backButton;
    private JButton createProjectButton;
    private JButton selectPathButton;
    private DocumentListener listener;

    public NewProjectWindow(ProjectsList list) {
        this.list = list;
        setTitle(Strings.get("applicationTitle") + " - " + Strings.get("new_project"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(contentPane);
        pack();
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        setLocation((displayMode.getWidth() - getWidth()) / 2, (displayMode.getHeight() - getHeight()) / 2);
        setResizable(false);
        try {
            setIconImage(ImageIO.read(getClass().getResource("resources/robot.png")));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        backButton.setText(Strings.get("go_back"));
        createProjectButton.setText(Strings.get("create_project"));
        createProjectButton.addActionListener((e) -> createProjectButtonPressed());
        selectPathButton.addActionListener((e) -> selectPathButtonPressed());
        backButton.addActionListener((e) -> backButtonPressed());
        listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkIfDataIsValid();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkIfDataIsValid();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkIfDataIsValid();
            }
        };
        nameTextField.getDocument().addDocumentListener(listener);
        pathTextField.getDocument().addDocumentListener(listener);
        setVisible(true);
    }

    private void checkIfDataIsValid() {
        String name = nameTextField.getText();
        String pathString = pathTextField.getText();
        if(!name.isEmpty() && !pathString.isEmpty()) {
            try {
                Path path = Paths.get(pathString);
                if((Files.exists(path) && Files.isDirectory(path) && isDirectoryEmpty(path)) || !Files.exists(path)) {
                    createProjectButton.setEnabled(true);
                } else createProjectButton.setEnabled(false);
            } catch(IOException ex) {
                createProjectButton.setEnabled(false);
            }
        } else createProjectButton.setEnabled(false);
    }

    private static boolean isDirectoryEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void createProjectButtonPressed() {
        Path path = Paths.get(pathTextField.getText());
        Path main = path.resolve("main.py");
        try {
            if(!Files.exists(path))
                Files.createDirectories(path);
            Files.createFile(main);
            list.add(new Project(nameTextField.getText(), pathTextField.getText()));
            list.save(ApplicationPaths.projectsList);
            dispose();
            new MainWindow(path, list);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private void selectPathButtonPressed() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int i = jFileChooser.showOpenDialog(this);
        if(i == JFileChooser.APPROVE_OPTION) {
            Path path = jFileChooser.getSelectedFile().toPath();
            pathTextField.setText(path.toAbsolutePath().toString());
            if(nameTextField.getText().isEmpty())
                nameTextField.setText(path.getFileName().toString());
        }
    }

    private void backButtonPressed() {
        dispose();
        new ProjectsWindow();
    }
}
