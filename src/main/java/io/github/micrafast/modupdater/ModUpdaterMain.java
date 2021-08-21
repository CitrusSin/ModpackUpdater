package io.github.micrafast.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.micrafast.modupdater.client.ClientConfig;
import io.github.micrafast.modupdater.client.UpdaterClient;
import io.github.micrafast.modupdater.server.ServerConfig;
import io.github.micrafast.modupdater.server.UpdaterServer;

import java.io.File;
import java.io.IOException;

public class ModUpdaterMain {
    public static final String SERVICE_NAME = "ModpackUpdateService";
    public static final String SERVICE_VER  = "1.2.000";

    public static final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        // Read arguments
        boolean isServer = false;
        for (String s : args) {
            if (s.equals("--server") || s.equals("-s")) {
                isServer = true;
            }
        }
        // Start server or client according to arguments
        if (isServer) {
            try {
                ServerConfig config = new ServerConfig();
                File serverConfigFile = new File("modupdater_server_config.json");
                if (!serverConfigFile.exists()) {
                    Utils.writeFile(serverConfigFile, "UTF-8", prettyGson.toJson(config));
                } else {
                    config = prettyGson.fromJson(Utils.readFile(serverConfigFile,"UTF-8"), ServerConfig.class);
                }
                new UpdaterServer(config).runServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ClientConfig config = new ClientConfig();
                File clientConfigFile = new File("modupdater_client_config.json");
                if (!clientConfigFile.exists()) {
                    Utils.writeFile(clientConfigFile, "UTF-8", prettyGson.toJson(config));
                } else {
                    config = prettyGson.fromJson(Utils.readFile(clientConfigFile,"UTF-8"), ClientConfig.class);
                }
                new UpdaterClient(config, clientConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
