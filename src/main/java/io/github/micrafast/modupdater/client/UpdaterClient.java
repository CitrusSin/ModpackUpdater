package io.github.micrafast.modupdater.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.ModUpdaterMain;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.client.controller.MainController;
import io.github.micrafast.modupdater.client.network.NetworkUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class UpdaterClient {
    private static UpdaterClient instance;

    protected Log log = LogFactory.getLog(getClass());

    ClientConfig config;
    MainController controller;

    private Gson prettyGson;
    private Gson gson;
    private File configFile;

    public UpdaterClient(ClientConfig config, File configFile) {
        prettyGson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        gson = new Gson();
        instance = this;
        this.config = config;
        this.configFile = configFile;
        controller = new MainController(config);
        if (config.synchronizeOnStart) {
            controller.startUpdateMods();
        }
    }

    public void updateMods() {
        try {
            String url = this.config.updateServerAddress;
            File modsFolder = new File(this.config.modsFolder);
            if (!modsFolder.exists()) {
                controller.popupWindow(ModUpdaterMain.language.get("popup.modsFolderDoesNotExist"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            while (url.endsWith("/")) {
                url = url.substring(0, url.length()-1);
            }

            String listUrl = url + "/mods/list";
            String downloadUrl = url + "/mods/downloads/";

            controller.outputLog(ModUpdaterMain.language.get("log.receivingModList"));
            ModManifest remoteManifest = gson.fromJson(NetworkUtils.getString(listUrl), ModManifest.class);
            List<Mod> localMods = Mod.getModList(modsFolder);
            List<Mod> optionalList = remoteManifest.optionalMods;
            List<Mod> standardList = remoteManifest.commonMods;

            // Add missing mods
            for (Mod mod : standardList) {
                if (!Utils.containsMod(localMods, mod)) {
                    File modFile = new File(modsFolder, mod.getFileName());
                    if (modFile.exists()) {
                        controller.outputLog(String.format(
                                ModUpdaterMain.language.get("log.alreadyExists"),
                                mod.getFileName()
                        ));
                    } else {
                        controller.outputLog(String.format(
                                ModUpdaterMain.language.get("log.startDownload"),
                                mod.getFileName()
                        ));
                    }
                    NetworkUtils.download(downloadUrl + URLEncoder.encode(mod.getFileName(), "UTF-8"), modFile);
                }
            }

            // Removing redundant mods
            for(Mod mod : localMods) {
                boolean condition;
                if (this.config.updateOptionalMods) {
                    condition = !Utils.containsMod(standardList, mod)
                            && (!Utils.containsMod(optionalList, mod));
                } else {
                    condition = (!Utils.containsMod(standardList, mod))
                            && (!Utils.containsMod(optionalList, mod))
                            && (!Utils.containsModByFileName(optionalList, mod));
                }
                if (condition) {
                    File modFile = new File(modsFolder, mod.getFileName());
                    controller.outputLog(String.format(
                            ModUpdaterMain.language.get("log.delete"),
                            mod.getFileName()
                    ));
                    modFile.delete();
                }
            }

            localMods = Mod.getModList(modsFolder);

            // Download optional mods
            if (this.config.updateOptionalMods) {
                for (Mod mod : optionalList) {
                    if (!Utils.containsMod(localMods, mod)) {
                        File modFile = new File(modsFolder, mod.getFileName());
                        controller.outputLog(String.format(
                                ModUpdaterMain.language.get("log.startDownloadOptional"),
                                mod.getFileName()
                        ));
                        NetworkUtils.download(downloadUrl + URLEncoder.encode(mod.getFileName(), "UTF-8"), modFile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            controller.outputLog(e.getLocalizedMessage());
        }
        controller.outputLog(ModUpdaterMain.language.get("log.updateComplete"));
    }

    public void saveConfig(ClientConfig config) throws IOException {
        this.config = config;
        Utils.writeFile(configFile, "UTF-8", prettyGson.toJson(config));
    }

    public static UpdaterClient getInstance() {
        return instance;
    }
}
