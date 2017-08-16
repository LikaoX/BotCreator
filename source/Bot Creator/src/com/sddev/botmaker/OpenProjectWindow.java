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

public class OpenProjectWindow extends JFrame{
    private JPanel contentPane;
    private JTextField nameTextField;
    private JTextField pathTextField;
    private JButton selectPathButton;
    private JButton backButton;
    private JButton openProjectButton;
    private DocumentListener listener;
    private ProjectsList list;


    public OpenProjectWindow(ProjectsList list) {
        this.list = list;
        setTitle(Strings.get("applicationTitle") + " - " + Strings.get("open_project"));
        backButton.setText(Strings.get("go_back"));
        openProjectButton.setText(Strings.get("open_project"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(contentPane);
        pack();
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        setLocation((displayMode.getWidth() - getWidth()) / 2, (displayMode.getHeight() - getHeight()) / 2);
        try {
            setIconImage(ImageIO.read(getClass().getResource("resources/robot.png")));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        setResizable(false);
        openProjectButton.addActionListener((e) -> openProjectButtonPressed());
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
                if(Files.exists(path) && Files.isDirectory(path) && !isDirectoryEmpty(path)) {
                    openProjectButton.setEnabled(true);
                } else openProjectButton.setEnabled(false);
            } catch(IOException ex) {
                openProjectButton.setEnabled(false);
            }
        } else openProjectButton.setEnabled(false);
    }

    private static boolean isDirectoryEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void openProjectButtonPressed() {
        Path path = Paths.get(pathTextField.getText());
        try {
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
