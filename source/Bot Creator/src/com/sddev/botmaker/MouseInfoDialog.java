package com.sddev.botmaker;

import javax.swing.*;
import java.awt.*;

public class MouseInfoDialog extends JDialog {
    private volatile boolean update;
    private GlobalListener.PointerInfo info;
    private JPanel contentPane;
    private JLabel xLabel;
    private JLabel yLabel;
    private JLabel dxLabel;
    private JLabel dyLabel;

    public MouseInfoDialog(GlobalListener.PointerInfo info) {
        this.info = info;
        setUndecorated(true);
        setAlwaysOnTop(true);
        setContentPane(contentPane);
        pack();
        Rectangle max = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setLocation(max.width - getWidth(), max.height - getHeight());
        update = true;
        new Thread(() -> {while(update) loop();}).start();
        setVisible(true);
    }

    private void loop() {
        xLabel.setText(String.valueOf(info.x2));
        yLabel.setText(String.valueOf(info.y2));
        dxLabel.setText(String.valueOf(info.x2 - info.x));
        dyLabel.setText(String.valueOf(info.y2 - info.y));
        try {
            Thread.sleep(30);
        } catch(InterruptedException ex) {

        }
    }

    @Override
    public void dispose() {
        update = false;
        super.dispose();
    }
}
