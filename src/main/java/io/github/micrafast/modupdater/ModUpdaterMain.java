package io.github.micrafast.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.micrafast.modupdater.client.ClientConfig;
import io.github.micrafast.modupdater.client.UpdaterClient;
import io.github.micrafast.modupdater.server.ServerConfig;
import io.github.micrafast.modupdater.server.UpdaterServer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class ModUpdaterMain {
    public static Map<String, String> language;
    public static final String SERVICE_NAME = "ModpackUpdateService";
    public static final String SERVICE_VER  = "1.0";

    public static void main(String[] args) {
        loadLanguage(Locale.getDefault());
        Gson prettyGson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
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
                new UpdaterServer(config);
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
                new UpdaterClient(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadLanguage(Locale locale) {
        InputStream inputStream =
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream(
                                String.format("assets/modupdater/lang/%s_%s.json",
                                        locale.getLanguage(),
                                        locale.getCountry()
                                )
                        );
        if (inputStream == null) {
            inputStream = ClassLoader.getSystemClassLoader()
                            .getResourceAsStream(
                                    String.format("assets/modupdater/lang/%s.json",
                                            locale.getLanguage()
                                    )
                            );
        }
        if (inputStream == null) {
            inputStream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(
                            "assets/modupdater/lang/en_US.json"
                    );
        }
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Gson mapgson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        language = mapgson.fromJson(reader, type);
    }
}
