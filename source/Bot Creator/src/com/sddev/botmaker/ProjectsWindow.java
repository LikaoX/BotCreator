package com.sddev.botmaker;

import org.python.antlr.ast.Str;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectsWindow extends JFrame {

    private JPanel mainPanel;
    private JList list1;
    private JButton newProjectButton;
    private JButton openProjectButton;
    private JButton exitButton;
    private JLabel titleLabel;
    private JLabel versionLabel;
    private JScrollPane scroll;
    private DefaultListModel<ProjectsListCell> listModel;
    private ProjectsList projects;

    public ProjectsWindow() {
        setTitle(Strings.get("applicationTitle"));
        titleLabel.setText(Strings.get("applicationTitleWelcomeScreen"));
        versionLabel.setText(Strings.get("applicationVersionWelcomeScreen"));
        newProjectButton.setText(Strings.get("new_project"));
        openProjectButton.setText(Strings.get("open_project"));
        exitButton.setText(Strings.get("exit2"));
        scroll.setViewportView(list1);
        setContentPane(mainPanel);
        pack();
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = graphicsDevice.getDisplayMode();
        setLocation(displayMode.getWidth() / 2 - getSize().width / 2, displayMode.getHeight() / 2 - getSize().height / 2);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        try {
            setIconImage(ImageIO.read(getClass().getResource("resources/robot.png")));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        setupButtons();
        listModel = new DefaultListModel<>();
        list1.setModel(listModel);
        list1.setCellRenderer(new CellRenderer());
        MouseAdapter mouseAdapter = new MouseAdapter() {

            int lastIndex = -1;

            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if(index != -1) {
                    getElement(index).getCell().setBackground(ProjectsListCell.pressedBackground);
                    list1.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if(index != -1) {
                    ProjectsListCell element = getElement(index);
                    element.getCell().setBackground(ProjectsListCell.hoverBackground);
                    list1.repaint();
                    if(element.runProject(projects))
                        ProjectsWindow.this.dispose();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if(index != -1) {
                    getElement(index).getCell().setBackground(ProjectsListCell.defaultBackground);
                    list1.repaint();
                    lastIndex = -1;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if(index != lastIndex) {
                    if(index != -1) {
                        getElement(index).getCell().setBackground(ProjectsListCell.hoverBackground);
                    }
                    if(lastIndex != -1) {
                        getElement(lastIndex).getCell().setBackground(ProjectsListCell.defaultBackground);
                    }
                    lastIndex = index;
                    list1.repaint();
                }
            }

            private ProjectsListCell getElement(int index) {
                return (ProjectsListCell) list1.getModel().getElementAt(index);
            }

            private int locationToIndex(Point point) {
                if(list1.getModel().getSize() > 0) {
                    int i = point.y / getElement(0).getCell().getPreferredSize().height;
                    return i >= list1.getModel().getSize() ? -1 : i;
                } else return -1;
            }
        };
        list1.addMouseMotionListener(mouseAdapter);
        list1.addMouseListener(mouseAdapter);
        if(loadList()) {
            for(Project project : projects) {
                Path path = Paths.get(project.getDirectory());
                if(Files.exists(path) && Files.isDirectory(path))
                    listModel.addElement(new ProjectsListCell(project.getName(), path));
            }
        } else {
            try {
                if(!Files.exists(ApplicationPaths.applicationDirectory))
                    Files.createDirectory(ApplicationPaths.applicationDirectory);
                projects.save(ApplicationPaths.projectsList);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        setVisible(true);
        setAlwaysOnTop(true);
        setAlwaysOnTop(false);
    }

    private boolean loadList() {
        try {
            if(Files.exists(ApplicationPaths.applicationDirectory) && Files.exists(ApplicationPaths.projectsList)) {
                projects = ProjectsList.load(ApplicationPaths.projectsList);
                return true;
            } else {
                projects = new ProjectsList();
            }
        } catch(IOException | ClassNotFoundException ex) {
            projects = new ProjectsList();
        }
        return false;
    }

    private void setupButtons() {
        Color defaultButtonColor = newProjectButton.getForeground();
        Color hoverButtonColor = new Color(183, 153, 15);
        Color clickButtonColor = new Color(123, 111, 36);
        MouseAdapter buttonsMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(e, hoverButtonColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(e, defaultButtonColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setForeground(e, clickButtonColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setForeground(e, hoverButtonColor);
            }

            private void setForeground(MouseEvent e, Color color) {
                Object source = e.getSource();
                if(source instanceof JButton) {
                    JButton jButton = (JButton) source;
                    jButton.setForeground(color);
                }
            }
        };
        newProjectButton.addMouseListener(buttonsMouseAdapter);
        openProjectButton.addMouseListener(buttonsMouseAdapter);
        exitButton.addMouseListener(buttonsMouseAdapter);
        newProjectButton.addActionListener((e) -> newProjectButtonPressed(e));
        openProjectButton.addActionListener((e) -> openProjectButtonPressed(e));
        exitButton.addActionListener((e) -> System.exit(0));
    }

    private void newProjectButtonPressed(ActionEvent event) {
        dispose();
        new NewProjectWindow(projects);
    }

    private void openProjectButtonPressed(ActionEvent event) {
        dispose();
        new OpenProjectWindow(projects);
    }

    private static class CellRenderer implements ListCellRenderer<ProjectsListCell> {
        @Override
        public Component getListCellRendererComponent(JList<? extends ProjectsListCell> list, ProjectsListCell value, int index, boolean isSelected, boolean cellHasFocus) {
            return value.getCell();
        }
    }
}
