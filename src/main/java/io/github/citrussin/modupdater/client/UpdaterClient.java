package io.github.citrussin.modupdater.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.citrussin.modupdater.ModUpdaterMain;
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

    private static final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

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
                Utils.writeFile(clientConfigFile, "UTF-8", prettyGson.toJson(config));
            } else {
                config = prettyGson.fromJson(Utils.readFile(clientConfigFile, "UTF-8"), ClientConfig.class);
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
        Utils.writeFile(configFile, "UTF-8", ModUpdaterMain.prettyGson.toJson(config));
    }

    public static UpdaterClient getInstance() {
        return instance;
    }
}
