package io.github.micrafast.modupdater.server.curseforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.cfapi.CFMLink;
import io.github.micrafast.modupdater.cfapi.CurseForgeManifest;
import io.github.micrafast.modupdater.cfapi.CurseForgeMod;
import io.github.micrafast.modupdater.server.ServerConfig;
import io.github.micrafast.modupdater.server.UpdaterServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CurseForgeConfigurator {
    private CurseForgeManifest manifest;
    private ServerConfig serverConfig;

    private final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Log log = LogFactory.getLog(CurseForgeConfigurator.class);

    public CurseForgeConfigurator(File jsonFile) throws IOException {
        String json = Utils.readFile(jsonFile, "UTF-8");
        manifest = CurseForgeManifest.fromJson(json);

        serverConfig = new ServerConfig();
        File serverConfigFile = new File(UpdaterServer.CONFIG_FILE_NAME);
        if (!serverConfigFile.exists()) {
            Utils.writeFile(serverConfigFile, "UTF-8", prettyGson.toJson(serverConfig));
        } else {
            serverConfig = prettyGson.fromJson(Utils.readFile(serverConfigFile,"UTF-8"), ServerConfig.class);
        }

        File    dir1 = new File(serverConfig.commonModsFolder),
                dir2 = new File(serverConfig.optionalModsFolder);
        if (!dir1.isDirectory()) {
            dir1.mkdirs();
        }
        if (!dir2.isDirectory()) {
            dir2.mkdirs();
        }
    }

    public void initializeNewLink(){
        ModManifest localManifest = new ModManifest(serverConfig.commonModsFolder, serverConfig.optionalModsFolder);
        List<CFMLink> links = new ArrayList<>(manifest.files.size());
        File modsFolder = new File(serverConfig.commonModsFolder);
        for (CurseForgeMod mod : manifest.files) {
            try {
                if (localManifest.searchFileName(mod.getName()) == null) {
                    log.info("Downloading mod " + mod.getName());
                    try {
                        links.add(mod.download(modsFolder));
                    } catch (IOException e) {
                        log.error("Failed to download mod " + mod.getName());
                    }
                } else {
                    log.info(String.format("%s already exists, set up link.", mod.getName()));
                    Mod mod2 = localManifest.searchFileName(mod.getName());
                    links.add(new CFMLink(mod2, mod));
                }
            } catch (Exception e) {
                log.error("Problem occured in downloading mod. Skipping...", e);
            }
        }

        File linkListFile = new File(CFModManager.DEFAULT_LINKLIST_CONFIG_FILE);
        if (!linkListFile.exists()) {
            try {
                if (!linkListFile.createNewFile()) {
                    log.error("Unable to create link list file");
                }
            } catch (IOException e) {
                log.error("Unable to create link list file", e);
            }
        }
        try {
            Utils.writeFile(linkListFile, "UTF-8", prettyGson.toJson(links));
        } catch (IOException e) {
            log.error("Unable to write link list file", e);
        }
    }
}
