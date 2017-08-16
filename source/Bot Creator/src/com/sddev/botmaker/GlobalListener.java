package com.sddev.botmaker;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class GlobalListener implements NativeKeyListener, NativeMouseInputListener {

    private Record record;
    private long time = -1;
    private PointerInfo pointerInfo = new PointerInfo();

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        int key = KeyUtils.vcToVk(nativeKeyEvent.getKeyCode());
        if(key >= KeyEvent.VK_F1 && key <= KeyEvent.VK_F24)
            functionKeyPressed(key - KeyEvent.VK_F1 + 1);
        else {
            if(record != null) {
                if(time != -1) {
                    record.add(new Record.SleepAction(System.currentTimeMillis() - time));
                }
                time = System.currentTimeMillis();
                record.add(new Record.KeyboardAction(key, Record.KeyboardActionType.press));
            }
        }
        if(key == KeyEvent.VK_F2) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            pointerInfo.x = p.x;
            pointerInfo.y = p.y;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        int key = KeyUtils.vcToVk(nativeKeyEvent.getKeyCode());
        if(!(key > KeyEvent.VK_F1 && key < KeyEvent.VK_F24)) {
            if(record != null) {
                if(time != -1) {
                    record.add(new Record.SleepAction(System.currentTimeMillis() - time));
                }
                time = System.currentTimeMillis();
                record.add(new Record.KeyboardAction(key, Record.KeyboardActionType.release));
            }
        }
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        int x = nativeMouseEvent.getX();
        int y = nativeMouseEvent.getY();
        int button = nativeMouseEvent.getButton();
        if(record != null) {
            if(time != -1) {
                record.add(new Record.SleepAction(System.currentTimeMillis() - time));
            }
            time = System.currentTimeMillis();
            record.add(new Record.MouseAction(x, y, button, Record.MouseActionType.press));
        }

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        int x = nativeMouseEvent.getX();
        int y = nativeMouseEvent.getY();
        int button = nativeMouseEvent.getButton();
        if(record != null) {
            if(time != -1) {
                record.add(new Record.SleepAction(System.currentTimeMillis() - time));
            }
            time = System.currentTimeMillis();
            record.add(new Record.MouseAction(x, y, button, Record.MouseActionType.release));
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        pointerInfo.x2 = nativeMouseEvent.getX();
        pointerInfo.y2 = nativeMouseEvent.getY();

    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    public abstract void functionKeyPressed(int keyCode);

    public void startRecording() {
        this.record = new Record();
        time = -1;
    }

    public boolean isRecording() {
        return this.record != null;
    }

    public Record stopRecording() {
        try {
            return this.record;
        } finally {
            this.record = null;
        }
    }

    public PointerInfo getPointerInfo() {
        return pointerInfo;
    }

    public class PointerInfo {
        public int x, y, x2, y2;
    }
}
