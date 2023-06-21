package io.github.citrussin.modupdater.server;

import io.github.citrussin.modupdater.GsonManager;
import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.ModUpdaterMain;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.server.handlers.ModHashTransferHandler;
import io.github.citrussin.modupdater.server.handlers.ModListHandler;
import io.github.citrussin.modupdater.server.handlers.ModTransferHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;

public class Server {
    public static final String CONFIG_FILE_NAME = "modupdater_server_config.json";
    private static Server instance;

    protected final Log log = LogFactory.getLog(this.getClass());
    protected ModManifestManager manifestManager;
    HttpService service;
    ServerSocket serverSocket;
    ServerConfig config;

    public Server() throws IOException {
        instance = this;

        config = new ServerConfig();
        File serverConfigFile = new File(CONFIG_FILE_NAME);
        if (!serverConfigFile.exists()) {
            Utils.writeFile(serverConfigFile, GsonManager.prettyGson.toJson(config));
        } else {
            config = GsonManager.prettyGson.fromJson(Utils.readFile(serverConfigFile), ServerConfig.class);
        }

        checkConfig();
        manifestManager = new ModManifestManager(config);
    }

    public void runServer() throws IOException {
        UriHttpRequestHandlerMapper mapper = new UriHttpRequestHandlerMapper();
        mapper.register("/mods/list", new ModListHandler(config));
        mapper.register("/mods/downloads/*", new ModTransferHandler(config, manifestManager));
        for (MessageDigest hashAlgorithm : Mod.HASH_ALGORITHMS) {
            mapper.register(
                    String.format("/mods/downloads/%s/*", hashAlgorithm.getAlgorithm()),
                    new ModHashTransferHandler(config, manifestManager, hashAlgorithm)
            );
        }

        HttpProcessor processor = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer(ModUpdaterMain.SERVICE_NAME + "/" + ModUpdaterMain.SERVICE_VER))
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();
        this.service = new HttpService(processor, mapper);

        serverSocket = new ServerSocket(this.config.port);
        log.info(String.format("Server started listening at port %d with mods folder: %s", this.config.port, this.config.commonModsFolder));
        while (!Thread.interrupted()) {
            Socket socket = serverSocket.accept();
            DefaultBHttpServerConnection connection = new DefaultBHttpServerConnection(8*1024);
            connection.bind(socket);
            Thread t = new WorkerThread(this.service, connection);
            t.setDaemon(false);
            t.start();
        }
        manifestManager.close();
    }

    private boolean checkConfig() {
        File folder = new File(this.config.commonModsFolder);
        File folder2 = new File(this.config.optionalModsFolder);
        if (!folder.isDirectory()) {
            log.info(String.format("%s not exist, making directory...", folder.getAbsolutePath()));
            folder.mkdirs();
        }
        if (!folder2.isDirectory()) {
            log.info(String.format("%s not exist, making directory...", folder2.getAbsolutePath()));
            folder2.mkdirs();
        }
        return true;
    }

    public ModManifestManager getManifestManager() {
        return manifestManager;
    }

    public static Server getInstance() {
        return instance;
    }
}