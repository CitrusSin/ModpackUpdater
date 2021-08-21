package io.github.micrafast.modupdater.client;

import io.github.micrafast.modupdater.ModUpdaterMain;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.client.controller.MainController;
import io.github.micrafast.modupdater.client.utils.I18nUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class UpdaterClient {
    private static UpdaterClient instance;

    protected final Log log = LogFactory.getLog(getClass());

    ClientConfig config;
    MainController controller;

    private File configFile;

    public UpdaterClient(ClientConfig config, File configFile) {
        instance = this;
        I18nUtils.loadLanguage(Locale.getDefault());
        this.config = config;
        this.configFile = configFile;
        controller = new MainController(config);
        if (config.synchronizeOnStart) {
            controller.startUpdateMods();
        }
    }

    public void saveConfig(ClientConfig config) throws IOException {
        this.config = config;
        Utils.writeFile(configFile, "UTF-8", ModUpdaterMain.prettyGson.toJson(config));
    }

    public static UpdaterClient getInstance() {
        return instance;
    }
}
