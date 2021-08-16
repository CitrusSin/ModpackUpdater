package io.github.micrafast.modupdater.client.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainWindow extends JFrame {
    final Map<String, String> language;

    public JTextArea centerText;
    public JTextField addressField;
    public JTextField modsField;
    public JButton synchronizeButton;
    public JCheckBox autoUpdateBox;
    public JCheckBox updateOptionalBox;

    public MainWindow(Map<String, String> language) {
        this.language = language;
        this.construct();
    }

    protected void construct() {
        this.setTitle(language.get("window.title"));
        this.setSize(800, 550);
        this.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(3,3));
        this.setContentPane(panel);

        JScrollPane sp = new JScrollPane();
        centerText = new JTextArea();
        centerText.setEditable(false);
        sp.setViewportView(centerText);
        panel.add(BorderLayout.CENTER, sp);

        JPanel northPanel = new JPanel(new FlowLayout());
        panel.add(BorderLayout.NORTH, northPanel);

        JPanel  grid        = new JPanel(new GridLayout(1,2,3,0)),
                subGrid1    = new JPanel(new BorderLayout(3,0)),
                subGrid2    = new JPanel(new BorderLayout(3,0));
        addressField = new JTextField();
        modsField = new JTextField();
        JLabel  addressLabel    = new JLabel(language.get("window.address")),
                modsLabel       = new JLabel(language.get("window.mods"));
        synchronizeButton = new JButton(language.get("window.synchronize"));
        subGrid1.add(BorderLayout.WEST,     addressLabel);
        subGrid1.add(BorderLayout.CENTER,   addressField);
        subGrid2.add(BorderLayout.WEST,     modsLabel);
        subGrid2.add(BorderLayout.CENTER,   modsField);
        grid.add(subGrid1);
        grid.add(subGrid2);
        northPanel.add(grid);
        northPanel.add(synchronizeButton);

        JPanel checkBoxesPanel = new JPanel(new GridLayout(2,1,3,3));
        autoUpdateBox = new JCheckBox(language.get("window.autoUpdate"));
        updateOptionalBox = new JCheckBox(language.get("window.updateOptionalMods"));
        checkBoxesPanel.add(autoUpdateBox);
        checkBoxesPanel.add(updateOptionalBox);
        northPanel.add(checkBoxesPanel);

        this.setVisible(true);
    }
}
