package bot;

import com.sddev.botmaker.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class InputDialog {
    private RealDialog dialog;
    private volatile boolean waiting = true;
    public InputDialog() {
        dialog = new RealDialog();
    }
    public DataSet show() {
        dialog.setVisible(true);
        while(waiting);
        dialog.dispose();
        DataSet dataSet = new DataSet();
        for(String key : dialog.inputs.keySet()) {
            dataSet.put(key, getComponentValue(dialog.inputs.get(key)));
        }
        return dataSet;
    }

    private Object getComponentValue(Component component) {
        if(component instanceof JTextField) {
            return ((JTextField) component).getText();
        } else if(component instanceof JComboBox) {
            return ((JComboBox) component).getSelectedItem().toString();
        } else if(component instanceof JCheckBox) {
            return ((JCheckBox) component).isSelected();
        }
        return null;
    }

    public void add_text_input(String name) {
        dialog.addInput(name, new JTextField());
    }

    public void add_combo_box_input(String name, String[] items) {
        JComboBox<String> jComboBox = new JComboBox<>();
        jComboBox.setFocusable(false);
        for(String item : items)
            jComboBox.addItem(item);
        dialog.addInput(name, jComboBox);
    }

    public void add_check_box_input(String name) {
        add_check_box_input(name, false);
    }

    public void add_check_box_input(String name, boolean defaultValue) {
        JCheckBox checkBox = new JCheckBox(name);
        checkBox.setSelected(defaultValue);
        checkBox.setFocusable(false);
        dialog.addInput(name, checkBox);
    }

    private class RealDialog extends JDialog {
        private JPanel formPanel;
        private JButton button;
        private GridLayout layout;
        private HashMap<String, Component> inputs;
        public RealDialog() {
            inputs = new HashMap<>();
            setLayout(new BorderLayout());
            formPanel = new JPanel();
            button = new JButton(Strings.get("apply"));
            button.setFocusable(false);
            button.addActionListener((e) -> apply());
            add(formPanel, BorderLayout.CENTER);
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.add(button);
            add(buttonPanel, BorderLayout.SOUTH);
            layout = new GridLayout(0, 2, 4, 4);
            formPanel.setLayout(layout);
            formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            pack();
        }
        private void addInput(String name, Component component) {
            formPanel.add(new JLabel(component instanceof JCheckBox ? "" : name));
            formPanel.add(component);
            layout.setRows(layout.getRows() + 1);
            inputs.put(name, component);
            pack();
        }

        @Override
        public void pack() {
            super.pack();
            setMinimumSize(getSize());
        }

        private void apply() {
            for(String key : dialog.inputs.keySet()) {
                Component component = dialog.inputs.get(key);
                if(component instanceof JTextField && ((JTextField) component).getText().isEmpty())
                    return;
            }
            waiting = false;
        }
    }
}
