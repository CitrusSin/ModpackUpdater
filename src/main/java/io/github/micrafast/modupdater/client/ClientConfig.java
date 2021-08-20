package io.github.micrafast.modupdater.client;

import javax.swing.*;

public class ClientConfig {
    public String updateServerAddress = "http://example.com:14238";
    public String modsFolder = "./.minecraft/mods";
    public String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
    public boolean synchronizeOnStart = false;
    public boolean updateOptionalMods = false;
    public int maxThreadCount = 10;
}
