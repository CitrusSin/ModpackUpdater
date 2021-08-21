package io.github.micrafast.modupdater.client.ui.controls;

import io.github.micrafast.modupdater.client.utils.I18nUtils;

import javax.swing.*;
import java.awt.*;

public class ThreadListRenderer extends JLabel implements ListCellRenderer<Thread> {
    @Override
    public Component getListCellRendererComponent(JList<? extends Thread> list, Thread value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setText(I18nUtils.languageReplace(value.toString()));
        return this;
    }
}
