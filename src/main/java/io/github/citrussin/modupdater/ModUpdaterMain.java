package io.github.citrussin.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.citrussin.modupdater.client.UpdaterClient;
import io.github.citrussin.modupdater.server.UpdaterServer;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.CurseforgeRedirectionInitializer;

import java.io.File;
import java.io.IOException;

public class ModUpdaterMain {
    public static final String SERVICE_NAME = "ModpackUpdateService";
    public static final String SERVICE_VER  = "1.4.000";

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
                CurseforgeRedirectionInitializer configurator = new CurseforgeRedirectionInitializer(manifestFile);
                configurator.initializeNewLink();
                System.exit(0);
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
