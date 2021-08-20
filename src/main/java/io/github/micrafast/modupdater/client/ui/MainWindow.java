package io.github.micrafast.modupdater.client.ui;

import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.client.ui.controls.ObjectCheck;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainWindow extends JFrame {
    final Map<String, String> language;

    public JTextArea centerText;
    public JTextField addressField;
    public JTextField modsField;
    public JButton updateButton;
    public JButton refreshListButton;
    public JCheckBox autoUpdateBox;
    public JCheckBox updateOptionalBox;
    public JLabel statusLabel;
    public JList<Mod> commonModsList;
    public JList<Mod> optionalModsList;
    public JList<Mod> deleteModsList;
    public DefaultListModel<Mod> commonModsListModel;
    public DefaultListModel<Mod> optionalModsListModel;
    public DefaultListModel<Mod> deleteModsListModel;

    public MainWindow(Map<String, String> language) {
        this.language = language;
        this.construct();
    }

    // RT, this method is to construct the UI layout
    protected void construct() {
        this.setTitle(language.get("window.title"));
        this.setSize(800, 850);
        this.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(3,3));
        this.setContentPane(panel);

        JPanel splitPanelV = new JPanel(new BorderLayout(3,3));
        JPanel splitPanelH = new JPanel(new GridLayout(1,3));
        JPanel  commonPanel = new JPanel(new FlowLayout()),
                optionalPanel = new JPanel(new FlowLayout()),
                deletePanel = new JPanel(new FlowLayout());

        commonModsListModel = new DefaultListModel<>();
        optionalModsListModel = new DefaultListModel<>();
        deleteModsListModel = new DefaultListModel<>();
        commonModsList = new JList<>(commonModsListModel);
        optionalModsList = new JList<>(optionalModsListModel);
        deleteModsList = new JList<>(deleteModsListModel);
        commonModsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionalModsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deleteModsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane commonScroll = new JScrollPane(commonModsList),
                    optionalScroll = new JScrollPane(optionalModsList),
                    deleteScroll = new JScrollPane(deleteModsList);
        Dimension minimumSize = new Dimension(400,600);
        commonScroll.setMinimumSize(minimumSize);
        optionalPanel.setMinimumSize(minimumSize);
        deleteScroll.setMinimumSize(minimumSize);
        JLabel  commonLabel = new JLabel(language.get("window.modsCommon")),
                optionalLabel = new JLabel(language.get("window.modsOptional")),
                deleteLabel = new JLabel(language.get("window.modsDelete"));
        commonPanel.add(commonLabel);
        commonPanel.add(commonScroll);
        optionalPanel.add(optionalLabel);
        optionalPanel.add(optionalScroll);
        deletePanel.add(deleteLabel);
        deletePanel.add(deleteScroll);

        splitPanelH.add(commonPanel);
        splitPanelH.add(optionalPanel);
        splitPanelH.add(deletePanel);
        splitPanelV.add(splitPanelH, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane();
        centerText = new JTextArea();
        centerText.setEditable(false);
        sp.setViewportView(centerText);

        updateButton = new JButton(language.get("window.update"));
        splitPanelV.add(updateButton, BorderLayout.SOUTH);

        panel.add(BorderLayout.CENTER, splitPanelV);

        JPanel northPanel = new JPanel(new FlowLayout());
        panel.add(BorderLayout.NORTH, northPanel);

        JPanel  grid        = new JPanel(new GridLayout(1,2,3,0)),
                subGrid1    = new JPanel(new BorderLayout(3,0)),
                subGrid2    = new JPanel(new BorderLayout(3,0));
        addressField = new JTextField();
        modsField = new JTextField();
        addressField.setMaximumSize(new Dimension(300,40));
        modsField.setMaximumSize(new Dimension(300,40));
        JLabel  addressLabel    = new JLabel(language.get("window.address")),
                modsLabel       = new JLabel(language.get("window.mods"));
        refreshListButton = new JButton(language.get("window.refreshList"));
        subGrid1.add(BorderLayout.WEST,     addressLabel);
        subGrid1.add(BorderLayout.CENTER,   addressField);
        subGrid2.add(BorderLayout.WEST,     modsLabel);
        subGrid2.add(BorderLayout.CENTER,   modsField);
        grid.add(subGrid1);
        grid.add(subGrid2);
        northPanel.add(grid);
        northPanel.add(refreshListButton);

        JPanel checkBoxesPanel = new JPanel(new GridLayout(2,1,3,3));
        autoUpdateBox = new JCheckBox(language.get("window.autoUpdate"));
        updateOptionalBox = new JCheckBox(language.get("window.updateOptionalMods"));
        checkBoxesPanel.add(autoUpdateBox);
        checkBoxesPanel.add(updateOptionalBox);
        northPanel.add(checkBoxesPanel);

        JPanel statusBar = new JPanel();
        statusLabel = new JLabel();
        statusBar.add(statusLabel);
        panel.add(statusBar, BorderLayout.SOUTH);

        this.setVisible(true);
    }
}
