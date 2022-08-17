package io.github.citrussin.modupdater.client;

import io.github.citrussin.modupdater.GsonManager;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.client.controller.MainController;
import io.github.citrussin.modupdater.client.utils.I18nUtils;
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

    public UpdaterClient() {
        instance = this;
        I18nUtils.loadLanguage(Locale.getDefault());

        config = new ClientConfig();
        try {
            File clientConfigFile = new File("modupdater_client_config.json");
            if (!clientConfigFile.exists()) {
                Utils.writeFile(clientConfigFile, "UTF-8", GsonManager.prettyGson.toJson(config));
            } else {
                config = GsonManager.prettyGson.fromJson(Utils.readFile(clientConfigFile, "UTF-8"), ClientConfig.class);
            }
            this.configFile = clientConfigFile;
        } catch (Exception e) {
            log.error(e);
        }

        controller = new MainController(config);
        if (config.synchronizeOnStart) {
            controller.startUpdateMods();
        }
    }

    public void saveConfig(ClientConfig config) throws IOException {
        this.config = config;
        Utils.writeFile(configFile, "UTF-8", GsonManager.prettyGson.toJson(config));
    }

    public static UpdaterClient getInstance() {
        return instance;
    }
}
