package io.github.micrafast.modupdater.client.controller;

import io.github.micrafast.modupdater.ModUpdaterMain;
import io.github.micrafast.modupdater.client.ClientConfig;
import io.github.micrafast.modupdater.client.UpdaterClient;
import io.github.micrafast.modupdater.client.network.NetworkUtils;
import io.github.micrafast.modupdater.client.ui.MainWindow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MainController {
    protected Log log = LogFactory.getLog(getClass());
    MainWindow window;
    ClientConfig config;
    Map<String, String> language;

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
        window = new MainWindow(ModUpdaterMain.language);
        language = ModUpdaterMain.language;
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
        window.synchronizeButton.addActionListener(e -> startUpdateModsEDT());
        window.autoUpdateBox.addActionListener(e -> {
            syncConfigAndSave();
        });
        window.updateOptionalBox.addActionListener(e -> {
            syncConfigAndSave();
        });
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
        config.updateOptionalMods = window.updateOptionalBox.isSelected();
    }

    private void loadConfig() {
        window.addressField.setText(config.updateServerAddress);
        window.modsField.setText(config.modsFolder);
        window.autoUpdateBox.setSelected(config.synchronizeOnStart);
        window.updateOptionalBox.setSelected(config.updateOptionalMods);
    }

    private void setOperationsEnabled(boolean enabled) {
        window.synchronizeButton.setEnabled(enabled);
        window.modsField.setEnabled(enabled);
        window.addressField.setEnabled(enabled);
        window.autoUpdateBox.setEnabled(enabled);
        window.updateOptionalBox.setEnabled(enabled);
    }

    protected void startUpdateModsEDT() {
        syncConfig();
        // Disable these controls
        setOperationsEnabled(false);
        Thread updateThread = new Thread(() -> {
            Log log = LogFactory.getLog("updateThread");
            UpdaterClient client = UpdaterClient.getInstance();
            try {
                client.saveConfig(config);
            } catch (IOException e) {
                log.error("Save config failed: ", e);
            }
            client.updateMods();
            SwingUtilities.invokeLater(() -> {
                setOperationsEnabled(true);
            });
        });
        updateThread.start();
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
        Thread updateThread = new Thread(() -> {
            Log log = LogFactory.getLog("updateThread");
            UpdaterClient client = UpdaterClient.getInstance();
            try {
                client.saveConfig(config);
            } catch (IOException e) {
                log.error("Save config failed: ", e);
            }
            client.updateMods();
            SwingUtilities.invokeLater(() -> {
                setOperationsEnabled(true);
            });
        });
        updateThread.start();
    }

    public void popupWindow(String text, int type) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(window, text, language.get("popup.title.message"), type);
            });
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void outputLog(String text) {
        SwingUtilities.invokeLater(() -> window.centerText.append(text + "\n"));
    }
}
