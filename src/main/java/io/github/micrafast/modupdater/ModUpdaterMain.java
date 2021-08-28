package io.github.micrafast.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.micrafast.modupdater.client.UpdaterClient;
import io.github.micrafast.modupdater.server.UpdaterServer;
import io.github.micrafast.modupdater.server.curseforge.CurseForgeConfigurator;

import java.io.File;
import java.io.IOException;

public class ModUpdaterMain {
    public static final String SERVICE_NAME = "ModpackUpdateService";
    public static final String SERVICE_VER  = "1.3.000-p-01";

    public static final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        // Read arguments
        boolean isConfigurator = false;
        String configManifestFilename = "";
        boolean isServer = false;
        for (int i=0;i<args.length;i++) {
            String s = args[i];
            if (s.equals("--curseforgeConfigurator") || s.equals("--cfConfig") || s.equals("-f")) {
                if (i+1 < args.length) {
                    isConfigurator = true;
                    configManifestFilename = args[i+1];
                }
            } else if (s.equals("--server") || s.equals("-s")) {
                isServer = true;
            }
        }
        // Start server or client according to arguments
        if (isConfigurator) {
            System.out.println("Thanks to curseforge api provided by Gaz492");
            System.out.println("API Doc: https://curseforgeapi.docs.apiary.io/");
            try {
                File manifestFile = new File(configManifestFilename);
                CurseForgeConfigurator configurator = new CurseForgeConfigurator(manifestFile);
                configurator.initializeNewLink();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isServer) {
            try {
                new UpdaterServer().runServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new UpdaterClient();
        }
    }

}
