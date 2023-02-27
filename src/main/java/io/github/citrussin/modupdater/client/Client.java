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

public class Client {
    private static Client instance;

    protected final Log log = LogFactory.getLog(getClass());

    ClientConfig config;
    MainController controller;

    private File configFile;

    public Client() {
        instance = this;
        I18nUtils.loadLanguage(Locale.getDefault());

        config = new ClientConfig();
        try {
            File clientConfigFile = new File("modupdater_client_config.json");
            if (!clientConfigFile.exists()) {
                Utils.writeFile(clientConfigFile, GsonManager.prettyGson.toJson(config));
            } else {
                config = GsonManager.prettyGson.fromJson(Utils.readFile(clientConfigFile), ClientConfig.class);
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
        Utils.writeFile(configFile, GsonManager.prettyGson.toJson(config));
    }

    public static Client getInstance() {
        return instance;
    }
}
