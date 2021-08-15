package io.github.micrafast.modupdater.client.ui;

import javax.swing.*;
import java.util.Map;

public class MainWindow extends JFrame {
    final Map<String, String> language;

    public MainWindow(Map<String, String> language) {
        this.language = language;
        this.construct();
    }

    protected void construct() {
        this.setTitle(language.get("window.title"));
        this.setSize(800, 350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}
