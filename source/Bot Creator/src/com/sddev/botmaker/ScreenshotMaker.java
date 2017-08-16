package com.sddev.botmaker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class ScreenshotMaker {
    private Robot robot;
    private Rectangle rectangle;
    public ScreenshotMaker() {
        try {
            robot = new Robot();
        } catch(AWTException ex) {
            ex.printStackTrace();
        }
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        rectangle = new Rectangle(0, devices[0].getDisplayMode().getHeight());
        for(GraphicsDevice device : devices) {
            DisplayMode displayMode = device.getDisplayMode();
            rectangle.width += displayMode.getWidth();
        }
    }

    private BufferedImage screenshot;
    private volatile boolean finished;
    private Point point;

    public BufferedImage makeScreenshot() {
        BufferedImage fullScreen = robot.createScreenCapture(rectangle);
        new Thread(() -> makeFrame(fullScreen)).start();
        while(!finished);
        try {
            return screenshot;
        } finally {
            finished = false;
            screenshot = null;
        }
    }

    private void makeFrame(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        JLabel label = new JLabel(new ImageIcon(image)) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Point point1 = getMousePosition();
                g.setColor(new Color(50, 50, 50, 180));
                g.fillRect(0, 0, rectangle.width, rectangle.height);
                if(point != null && point1.x > point.x && point1.y > point.y) {
                    g.setClip(point.x, point.y, point1.x - point.x, point1.y - point.y);
                    g.drawImage(image, 0, 0, null);
                }
            }
        };
        label.setFocusable(true);
        label.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    point = e.getPoint();
                } else if(SwingUtilities.isRightMouseButton(e)) {
                    frame.dispose();
                    finished = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point point1 = label.getMousePosition();
                if(point != null && point1.x > point.x && point1.y > point.y) {
                    Rectangle rectangle = new Rectangle(point.x, point.y, point1.x - point.x, point1.y - point.y);
                    screenshot = new BufferedImage(rectangle.width, rectangle.height, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D graphics2D = screenshot.createGraphics();
                    graphics2D.drawImage(image, (int) -rectangle.getX(), (int) -rectangle.getY(), null);
                    graphics2D.dispose();
                    frame.dispose();
                    finished = true;
                }
            }
        });
        label.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                label.repaint();
            }
        });
        frame.setBounds(0, 0, rectangle.width, rectangle.height);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setUndecorated(true);
        frame.add(label);
        frame.setAlwaysOnTop(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                finished = true;
                frame.dispose();
            }
        });
        frame.setVisible(true);
    }
}
