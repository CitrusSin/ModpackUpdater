package io.github.citrussin.modupdater.server.redirection.setup;

import io.github.citrussin.modupdater.GsonManager;
import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.server.Server;
import io.github.citrussin.modupdater.server.ServerConfig;
import io.github.citrussin.modupdater.server.redirection.ModRedirectionProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class RedirectionInitalizer {
    private ServerConfig serverConfig;

    public RedirectionInitalizer() throws IOException {
        // Load configuration file
        serverConfig = new ServerConfig();
        File serverConfigFile = new File(Server.CONFIG_FILE_NAME);
        if (!serverConfigFile.exists()) {
            Utils.writeFile(serverConfigFile, GsonManager.prettyGson.toJson(serverConfig));
        } else {
            serverConfig = GsonManager.prettyGson
                    .fromJson(Utils.readFile(serverConfigFile), ServerConfig.class);
        }

        // Create mods folder
        File    dir1 = new File(serverConfig.commonModsFolder),
                dir2 = new File(serverConfig.optionalModsFolder);
        if (!dir1.isDirectory()) {
            dir1.mkdirs();
        }
        if (!dir2.isDirectory()) {
            dir2.mkdirs();
        }
    }

    protected abstract String urlFromLocalMod(Mod mod) throws IOException;

    protected List<ModRedirectionProvider> getProviderList() throws IOException {
        List<ModRedirectionProvider> providerList = new LinkedList<>();

        File dir1 = new File(serverConfig.commonModsFolder);
        File dir2 = new File(serverConfig.optionalModsFolder);

        FilenameFilter jarFilter = (dir, name) -> name.endsWith(".jar");
        File[] list1 = dir1.listFiles(jarFilter);
        File[] list2 = dir2.listFiles(jarFilter);

        if (list1 != null) {
            for (File modFile : list1) {
                try {
                    Mod mod = new Mod(modFile);
                    String url = urlFromLocalMod(mod);

                    if (url != null) {
                        providerList.add(new ModRedirectionProvider(mod.getHashValues(), url));
                    } else {
                        throw new Exception("Failed to get URL");
                    }
                } catch (Exception e) {
                    System.err.printf("Failed to get information of %s%n", modFile.getName());
                }
            }
        }

        if (list2 != null) {
            for (File modFile : list2) {
                try {
                    Mod mod = new Mod(modFile);
                    System.out.printf("Setting url for %s ver %s%n", mod.getModId(), mod.getVersion());
                    String url = urlFromLocalMod(mod);

                    if (url != null) {
                        providerList.add(new ModRedirectionProvider(mod.getHashValues(), url));
                    }
                } catch (Exception e) {
                    System.err.printf("Failed to get information of %s%n", modFile.getName());
                }
            }
        }

        return providerList;
    }

    public void initializeRedirections() throws IOException {
        List<ModRedirectionProvider> providerList = getProviderList();
        String json = GsonManager.prettyGson.toJson(providerList);
        File redirFile = new File(serverConfig.redirectionListPath);
        if (!redirFile.exists()) {
            redirFile.createNewFile();
        }
        Utils.writeFile(redirFile, json);
        System.out.println("Mod redirection set up complete!");
    }
}
