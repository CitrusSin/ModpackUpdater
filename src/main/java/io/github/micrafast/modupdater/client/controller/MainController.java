package io.github.micrafast.modupdater.client.controller;

import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.async.AsyncTaskQueueRunner;
import io.github.micrafast.modupdater.client.ClientConfig;
import io.github.micrafast.modupdater.client.UpdateStrategy;
import io.github.micrafast.modupdater.client.UpdaterClient;
import io.github.micrafast.modupdater.client.ui.MainWindow;
import io.github.micrafast.modupdater.client.utils.I18nUtils;
import io.github.micrafast.modupdater.network.NetworkUtils;
import io.github.micrafast.modupdater.network.TaskFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Warning: trash code dump :(
 * Controller code is really hard to write....
 * Don't know what is MVC structure huh :(
 * But it just works as a "controller" :P
 */
public class MainController {
    protected Log log = LogFactory.getLog(getClass());
    MainWindow window;
    ClientConfig config;
    UpdateStrategy strategy = null;

    public MainController(ClientConfig config) {
        this.config = config;
        try {
            UIManager.setLookAndFeel(config.lookAndFeel);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                log.error("Failed setting look and feel", ex);
            }
        }
        window = new MainWindow();
        this.initialize();
    }

    protected void initialize() {
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    NetworkUtils.closeClient();
                } catch (IOException ex) {
                    log.error(ex);
                }
                System.exit(0);
            }
        });
        loadConfig();
        window.refreshListButton.addActionListener(e -> {
            Thread t = new Thread(() -> {
                getStrategy();
                SwingUtilities.invokeLater(() -> {
                    setStatus("status.completeReceivingModList");
                    setOperationsEnabled(true);
                });
            });
            t.start();
        });
        window.updateButton.addActionListener(e -> startUpdateModsEDT());
        window.autoUpdateBox.addActionListener(e -> syncConfigAndSave());
        window.optionalModsList.addListSelectionListener(e -> writeModSelection());
        window.deleteModsList.addListSelectionListener(e -> writeModSelection());
    }

    private void getStrategy() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                syncConfigAndSave();
                setOperationsEnabled(false);
                setStatus("status.receivingModList");
            });
            strategy = new UpdateStrategy(
                    ModManifest.fromRemote(config.updateServerAddress),
                    new File(config.modsFolder),
                    config.maxThreadCount
            );
            SwingUtilities.invokeAndWait(this::updateStrategy);
        } catch (Exception ex) {
            log.error(ex);
            popupWindow("${error.receivingModList}" + ex.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeModSelection() {
        List<Mod> mods = new ArrayList<>();

        int[] optionalIndices = window.optionalModsList.getSelectedIndices();
        for (int index : optionalIndices) {
            mods.add(window.optionalModsListModel.elementAt(index));
        }
        for (Map.Entry<Mod, Boolean> entry : strategy.getOptionalMods().entrySet()) {
            strategy.getOptionalMods().replace(entry.getKey(), mods.contains(entry.getKey()));
        }
        mods.clear();

        int[] deleteIndices = window.deleteModsList.getSelectedIndices();
        for (int index : deleteIndices) {
            mods.add(window.deleteModsListModel.elementAt(index));
        }
        for (Map.Entry<Mod, Boolean> entry : strategy.getRemoveMods().entrySet()) {
            strategy.getRemoveMods().replace(entry.getKey(), mods.contains(entry.getKey()));
        }
        mods.clear();
    }

    private void updateStrategy() {
        window.commonModsListModel.clear();
        for (Map.Entry<Mod, Boolean> entry : strategy.getInstallMods().entrySet()) {
            window.commonModsListModel.addElement(entry.getKey());
        }
        window.commonModsList.updateUI();

        window.optionalModsListModel.clear();
        ArrayList<Integer> selectedIndex = new ArrayList<>();
        for (Map.Entry<Mod, Boolean> entry : strategy.getOptionalMods().entrySet()) {
            window.optionalModsListModel.addElement(entry.getKey());
            if (entry.getValue()) {
                selectedIndex.add(window.optionalModsListModel.indexOf(entry.getKey()));
            }
        }
        int[] indices = new int[selectedIndex.size()];
        for (int i=0;i<selectedIndex.size();i++) {
            indices[i] = selectedIndex.get(i);
        }
        window.optionalModsList.setSelectedIndices(indices);
        window.optionalModsList.updateUI();

        selectedIndex.clear();
        window.deleteModsListModel.clear();
        for (Map.Entry<Mod, Boolean> entry : strategy.getRemoveMods().entrySet()) {
            window.deleteModsListModel.addElement(entry.getKey());
            if (entry.getValue()) {
                selectedIndex.add(window.deleteModsListModel.indexOf(entry.getKey()));
            }
        }
        indices = new int[selectedIndex.size()];
        for (int i=0;i<selectedIndex.size();i++) {
            indices[i] = selectedIndex.get(i);
        }
        window.deleteModsList.setSelectedIndices(indices);
        window.deleteModsList.updateUI();
    }

    private void syncConfigAndSave() {
        syncConfig();
        try {
            UpdaterClient.getInstance().saveConfig(config);
        } catch (IOException ex) {
            log.error("Save config failed: ", ex);
        }
    }

    private void syncConfig() {
        config.updateServerAddress = window.addressField.getText();
        config.modsFolder = window.modsField.getText();
        config.synchronizeOnStart = window.autoUpdateBox.isSelected();
    }

    private void loadConfig() {
        window.addressField.setText(config.updateServerAddress);
        window.modsField.setText(config.modsFolder);
        window.autoUpdateBox.setSelected(config.synchronizeOnStart);
    }

    private void setOperationsEnabled(boolean enabled) {
        window.updateButton.setEnabled(enabled);
        window.modsField.setEnabled(enabled);
        window.addressField.setEnabled(enabled);
        window.autoUpdateBox.setEnabled(enabled);
        window.refreshListButton.setEnabled(enabled);
    }

    protected void startUpdateModsEDT() {
        syncConfig();
        // Disable these controls
        setOperationsEnabled(false);
        createUpdateThread().start();
    }

    public void startUpdateMods() {
        try {
            // Make sure it's thread safe
            SwingUtilities.invokeAndWait(() -> {
                syncConfig();
                // Disable these controls
                setOperationsEnabled(false);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            log.error("Exception happened in SwingUtilities.invokeAndWait(): ", e);
        }
        createUpdateThread().start();
    }

    private Thread createUpdateThread() {
        return new Thread(() -> {
            Log log = LogFactory.getLog("updateUIThread");
            UpdaterClient client = UpdaterClient.getInstance();
            try {
                if (strategy == null) {
                    getStrategy();
                }
                client.saveConfig(config);
                AsyncTaskQueueRunner<TaskFileOperation, String> tasksRunner = strategy.getTaskRunner();
                tasksRunner.runTaskQueue();
                while (tasksRunner.running()) {
                    SwingUtilities.invokeAndWait(() -> {
                        window.taskListModel.clear();
                        tasksRunner.forEachRunning(window.taskListModel::addElement);
                        /*
                        for (Thread t : strategy.downloadings) {
                            window.taskListModel.addElement(t);
                        }
                        */
                        window.taskList.updateUI();
                    });
                    Thread.sleep(100);
                }
                strategy.calculateDifferences();
                SwingUtilities.invokeLater(this::updateStrategy);
            } catch (IOException e) {
                log.error(e);
                popupWindow("${error.downloadingMod}"+e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (InvocationTargetException | InterruptedException ignore) {
            }
            SwingUtilities.invokeLater(() -> {
                window.taskListModel.clear();
                window.taskList.updateUI();
                setStatus("status.updateComplete");
                setOperationsEnabled(true);
            });
        });
    }

    public void popupWindow(String text, int type) {
        try {
            SwingUtilities.invokeAndWait(() -> JOptionPane.showMessageDialog(window, I18nUtils.localize(text), I18nUtils.getContext("popup.title.message"), type));
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected void setStatus(String langKey) {
        window.statusLabel.setText(I18nUtils.getContext(langKey));
    }
}
