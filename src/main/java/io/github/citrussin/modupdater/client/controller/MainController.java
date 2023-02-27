package io.github.citrussin.modupdater.client.controller;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.ModManifest;
import io.github.citrussin.modupdater.async.TaskQueue;
import io.github.citrussin.modupdater.client.ClientConfig;
import io.github.citrussin.modupdater.client.UpdateStrategy;
import io.github.citrussin.modupdater.client.Client;
import io.github.citrussin.modupdater.client.ui.MainWindow;
import io.github.citrussin.modupdater.client.utils.I18nUtils;
import io.github.citrussin.modupdater.network.NetworkUtils;
import io.github.citrussin.modupdater.network.TaskFileOperation;
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
                    setControlsEnabled(true);
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
                setControlsEnabled(false);
                setStatus("status.receivingModList");
            });
            strategy = new UpdateStrategy(
                    ModManifest.fromRemote(config.updateServerAddress),
                    new File(config.modsFolder),
                    config.maxThreadCount
            );
            SwingUtilities.invokeAndWait(this::loadStrategy);
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

    private void loadStrategy() {
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
            Client.getInstance().saveConfig(config);
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

    private void setControlsEnabled(boolean enabled) {
        window.updateButton.setEnabled(enabled);
        window.modsField.setEnabled(enabled);
        window.addressField.setEnabled(enabled);
        window.autoUpdateBox.setEnabled(enabled);
        window.refreshListButton.setEnabled(enabled);
    }

    protected void startUpdateModsEDT() {
        syncConfig();
        // Disable these controls
        setControlsEnabled(false);
        createUpdateThread().start();
    }

    public void startUpdateMods() {
        try {
            // Make sure it's thread safe
            SwingUtilities.invokeAndWait(() -> {
                syncConfig();
                // Disable these controls
                setControlsEnabled(false);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            log.error("Exception occurred in SwingUtilities.invokeAndWait(): ", e);
        }
        createUpdateThread().start();
    }

    private Thread createUpdateThread() {
        return new Thread(() -> {
            Log log = LogFactory.getLog("updateUIThread");
            Client client = Client.getInstance();
            try {
                if (strategy == null) {
                    getStrategy();
                }
                client.saveConfig(config);
                TaskQueue<TaskFileOperation, String> tasksRunner = strategy.getTaskQueue();
                tasksRunner.addExceptionCallback((t, e) -> {
                    log.error(e);
                });
                tasksRunner.runTaskQueue();
                while (tasksRunner.running()) {
                    SwingUtilities.invokeAndWait(() -> {
                        window.taskListModel.clear();
                        tasksRunner.forEachRunning(window.taskListModel::addElement);
                        window.taskList.updateUI();
                    });
                    Thread.sleep(100);
                }
                strategy.calculateDifferences();
                Throwable[] exceptions = tasksRunner.getExceptionsThrown();
                if (exceptions.length > 0) {
                    StringBuilder exceptionInfoBuilder = new StringBuilder();
                    for (Throwable exception : exceptions) {
                        exceptionInfoBuilder.append(exception.getMessage());
                        exceptionInfoBuilder.append('\n');
                    }
                    String excInfo = exceptionInfoBuilder.toString();
                    popupWindow("${error.downloadingMod}\n" + excInfo, JOptionPane.ERROR_MESSAGE);
                }
                SwingUtilities.invokeLater(this::loadStrategy);
            } catch (IOException e) {
                log.error(e);
                popupWindow("${error.downloadingMod}"+e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (InvocationTargetException | InterruptedException ignore) {
            }
            SwingUtilities.invokeLater(() -> {
                window.taskListModel.clear();
                window.taskList.updateUI();
                setStatus("status.updateComplete");
                setControlsEnabled(true);
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
