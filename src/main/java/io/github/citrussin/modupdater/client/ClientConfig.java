package io.github.citrussin.modupdater.client;

import com.google.gson.annotations.Expose;

import javax.swing.*;

public class ClientConfig {
    @Expose
    public String updateServerAddress = "http://example.com:14238";
    @Expose
    public String modsFolder = "./.minecraft/mods";
    @Expose
    public String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
    @Expose
    public boolean synchronizeOnStart = false;
    @Expose
    public int maxThreadCount = 10;
}
