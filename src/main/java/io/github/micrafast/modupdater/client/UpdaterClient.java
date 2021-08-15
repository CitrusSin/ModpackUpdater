package io.github.micrafast.modupdater.client;

import io.github.micrafast.modupdater.ModUpdaterMain;
import io.github.micrafast.modupdater.client.ui.MainWindow;

public class UpdaterClient {
    private static UpdaterClient instance;


    ClientConfig config;
    MainWindow window;

    public UpdaterClient(ClientConfig config) {
        instance = this;
        this.config = config;
        window = new MainWindow(ModUpdaterMain.language);
    }

    public static UpdaterClient getInstance() {
        return instance;
    }
}
