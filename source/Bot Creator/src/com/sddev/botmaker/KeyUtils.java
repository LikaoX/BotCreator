package com.sddev.botmaker;

import org.jnativehook.keyboard.NativeKeyEvent;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class KeyUtils {

    public static final int vcToVk(int k) {
        try {
            Field[] fieldsVC = NativeKeyEvent.class.getFields();
            Field[] fieldsVK = KeyEvent.class.getFields();
            for(Field fieldVC : fieldsVC) {
                if(Modifier.isStatic(fieldVC.getModifiers()) && fieldVC.getType() == int.class && (int) fieldVC.get(null) == k) {
                    String name = fieldVC.getName();
                    name = name.substring(name.indexOf("_") + 1);
                    for (Field fieldVK : fieldsVK) {
                        if(Modifier.isStatic(fieldVK.getModifiers()) && fieldVK.getType() == int.class) {
                            String name2 = fieldVK.getName();
                            name2 = name2.substring(name2.indexOf("_") + 1);
                            if(name.equals(name2)) {
                                return (int) fieldVK.get(null);
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return k;
    }
}
