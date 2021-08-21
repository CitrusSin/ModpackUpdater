package io.github.micrafast.modupdater.client.ui.controls;

import javax.swing.*;
import java.awt.*;

public class ObjectCheck<T> extends JCheckBox implements ListCellRenderer<T> {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        this.setText(value.toString());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        this.setSelected(isSelected);
        return this;
    }
}
