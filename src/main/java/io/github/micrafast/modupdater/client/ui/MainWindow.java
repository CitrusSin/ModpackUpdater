package io.github.micrafast.modupdater.client.ui;

import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.client.ui.controls.ObjectCheck;
import io.github.micrafast.modupdater.client.ui.controls.ThreadListRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainWindow extends JFrame {
    final Map<String, String> language;

    public JTextField addressField;
    public JTextField modsField;
    public JButton updateButton;
    public JButton refreshListButton;
    public JCheckBox autoUpdateBox;
    public JLabel statusLabel;
    public JList<Mod> commonModsList;
    public JList<Mod> optionalModsList;
    public JList<Mod> deleteModsList;
    public JList<Thread> taskList;
    public DefaultListModel<Mod> commonModsListModel;
    public DefaultListModel<Mod> optionalModsListModel;
    public DefaultListModel<Mod> deleteModsListModel;
    public DefaultListModel<Thread> taskListModel;

    public MainWindow(Map<String, String> language) {
        this.language = language;
        this.construct();
    }

    // RT, this method is to construct the UI layout
    protected void construct() {
        this.setTitle(language.get("window.title"));
        this.setSize(800, 850);
        this.setLocationRelativeTo(null);

        //JPanel panel = new JPanel(new GridBagLayout());
        //this.setContentPane(panel);

        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        JPanel upperPanel = makeUpperPanel();
        GridBagConstraints upperPanelConst = new GridBagConstraints();
        upperPanelConst.fill        = GridBagConstraints.BOTH;
        upperPanelConst.gridx       = 0;
        upperPanelConst.gridy       = 0;
        upperPanelConst.gridwidth   = 1;
        upperPanelConst.gridheight  = 2;
        this.add(upperPanel, upperPanelConst);

        JPanel centerPanel = makeCenterPanel();
        GridBagConstraints centerPanelConst = new GridBagConstraints();
        centerPanelConst.fill       = GridBagConstraints.BOTH;
        centerPanelConst.gridx      = 0;
        centerPanelConst.gridy      = 2;
        centerPanelConst.gridwidth  = 1;
        centerPanelConst.gridheight = 6;
        centerPanelConst.weightx = 1;
        centerPanelConst.weighty = 1;
        this.add(centerPanel, centerPanelConst);

        JPanel statusBar = makeStatusBar();
        GridBagConstraints statusBarConst = new GridBagConstraints();
        statusBarConst.fill         = GridBagConstraints.BOTH;
        statusBarConst.gridx        = 0;
        statusBarConst.gridy        = 8;
        statusBarConst.gridwidth    = 1;
        statusBarConst.gridheight   = 1;
        this.add(statusBar, statusBarConst);

        this.setVisible(true);
    }

    private JPanel makeUpperPanel() {
        JPanel upperPanel = new JPanel(new FlowLayout());

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
        upperPanel.add(grid);
        upperPanel.add(refreshListButton);

        autoUpdateBox = new JCheckBox(language.get("window.autoUpdate"));
        upperPanel.add(autoUpdateBox);
        return upperPanel;
    }

    private JPanel makeCenterPanel() {
        JPanel centerPanel = new JPanel();
        GridBagLayout centerGridBag = new GridBagLayout();
        centerPanel.setLayout(centerGridBag);

        JPanel  commonPanel = new JPanel(new BorderLayout(3, 3)),
                optionalPanel = new JPanel(new BorderLayout(3, 3)),
                deletePanel = new JPanel(new BorderLayout(3, 3));

        commonModsListModel = new DefaultListModel<>();
        optionalModsListModel = new DefaultListModel<>();
        deleteModsListModel = new DefaultListModel<>();
        commonModsList = new JList<>(commonModsListModel);
        optionalModsList = new JList<>(optionalModsListModel);
        deleteModsList = new JList<>(deleteModsListModel);
        commonModsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionalModsList.setCellRenderer(new ObjectCheck<>());
        optionalModsList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
        optionalModsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deleteModsList.setCellRenderer(new ObjectCheck<>());
        deleteModsList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
        deleteModsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane commonScroll = new JScrollPane(commonModsList),
                optionalScroll = new JScrollPane(optionalModsList),
                deleteScroll = new JScrollPane(deleteModsList);
        JLabel  commonLabel = new JLabel(language.get("window.modsCommon")),
                optionalLabel = new JLabel(language.get("window.modsOptional")),
                deleteLabel = new JLabel(language.get("window.modsDelete"));
        commonPanel.add(commonLabel, BorderLayout.NORTH);
        commonPanel.add(commonScroll, BorderLayout.CENTER);
        optionalPanel.add(optionalLabel, BorderLayout.NORTH);
        optionalPanel.add(optionalScroll, BorderLayout.CENTER);
        deletePanel.add(deleteLabel, BorderLayout.NORTH);
        deletePanel.add(deleteScroll, BorderLayout.CENTER);

        GridBagConstraints  commonConst     = new GridBagConstraints(),
                optionalConst   = new GridBagConstraints(),
                deleteConst     = new GridBagConstraints();
        commonConst.fill    = GridBagConstraints.BOTH;
        optionalConst.fill  = GridBagConstraints.BOTH;
        deleteConst.fill    = GridBagConstraints.BOTH;

        commonConst.gridx       = 0;
        commonConst.gridy       = 0;
        commonConst.gridwidth   = 1;
        commonConst.gridheight  = 3;
        commonConst.weightx     = 1;
        commonConst.weighty     = 1;

        optionalConst.gridx         = 1;
        optionalConst.gridy         = 0;
        optionalConst.gridwidth     = 1;
        optionalConst.gridheight    = 3;
        optionalConst.weightx       = 1;
        optionalConst.weighty       = 1;

        deleteConst.gridx       = 2;
        deleteConst.gridy       = 0;
        deleteConst.gridwidth   = 1;
        deleteConst.gridheight  = 3;
        deleteConst.weightx     = 1;
        deleteConst.weighty     = 1;

        centerPanel.add(commonPanel, commonConst);
        centerPanel.add(optionalPanel, optionalConst);
        centerPanel.add(deletePanel, deleteConst);

        JPanel taskListPanel = new JPanel(new BorderLayout(3,3));
        JLabel taskListLabel = new JLabel(language.get("window.taskList"));
        taskListPanel.add(taskListLabel, BorderLayout.NORTH);

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new ThreadListRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane taskListScroll = new JScrollPane(taskList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        taskListPanel.add(taskListScroll, BorderLayout.CENTER);

        GridBagConstraints taskListConst = new GridBagConstraints();
        taskListConst.fill          = GridBagConstraints.BOTH;
        taskListConst.gridx         = 0;
        taskListConst.gridy         = 3;
        taskListConst.gridwidth     = 3;
        taskListConst.gridheight    = 3;
        taskListConst.weightx       = 1;
        taskListConst.weighty       = 1;
        centerPanel.add(taskListPanel, taskListConst);

        updateButton = new JButton(language.get("window.update"));
        GridBagConstraints updateButtonConst = new GridBagConstraints();
        updateButtonConst.fill          = GridBagConstraints.BOTH;
        updateButtonConst.gridx         = 0;
        updateButtonConst.gridy         = 6;
        updateButtonConst.gridwidth     = 3;
        updateButtonConst.gridheight    = 1;
        centerPanel.add(updateButton, updateButtonConst);
        return centerPanel;
    }

    private JPanel makeStatusBar() {
        JPanel statusBar = new JPanel();
        statusLabel = new JLabel();
        statusBar.add(statusLabel);
        return statusBar;
    }
}
