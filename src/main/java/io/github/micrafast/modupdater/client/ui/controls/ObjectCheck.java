package io.github.micrafast.modupdater.client.ui.controls;

import javax.swing.*;

public class ObjectCheck<T> extends JCheckBox {
    private T item;

    public ObjectCheck(T item) {
        this.item = item;
        this.setText(item.toString());
    }

    @Override
    public void setText(String text) {
        super.setText(this.item.toString());
    }

    public T getItem() {
        return this.item;
    }
}
