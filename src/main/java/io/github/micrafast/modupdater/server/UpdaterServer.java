package io.github.micrafast.modupdater.server;

import io.github.micrafast.modupdater.ModUpdaterMain;
import io.github.micrafast.modupdater.server.handlers.ModListHandler;
import io.github.micrafast.modupdater.server.handlers.ModTransferHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UpdaterServer {
    private static UpdaterServer instance;

    protected final Log log = LogFactory.getLog(this.getClass());
    HttpService service;
    ServerSocket serverSocket;
    final ServerConfig config;

    public UpdaterServer(ServerConfig config) throws IOException {
        this.config = config;
        instance = this;
        startServer();
    }

    private void startServer() throws IOException {
        if (!checkConfig()) {
            return;
        }

        UriHttpRequestHandlerMapper mapper = new UriHttpRequestHandlerMapper();
        mapper.register("/mods/list", new ModListHandler(config));
        mapper.register("/mods/downloads/*", new ModTransferHandler(config));

        HttpProcessor processor = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer(ModUpdaterMain.SERVICE_NAME + "/" + ModUpdaterMain.SERVICE_VER))
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();
        this.service = new HttpService(processor, mapper);

        serverSocket = new ServerSocket(this.config.port);
        log.info(String.format("Server started listening at port %d with mods folder: %s", this.config.port, this.config.modsFolder));
        while (!Thread.interrupted()) {
            Socket socket = serverSocket.accept();
            DefaultBHttpServerConnection connection = new DefaultBHttpServerConnection(8*1024);
            connection.bind(socket);
            Thread t = new WorkerThread(this.service, connection);
            t.setDaemon(false);
            t.start();
        }
    }

    private boolean checkConfig() {
        File folder = new File(this.config.modsFolder);
        if (!folder.isDirectory()) {
            log.error("Check configuration failed: Mod directory does not exist");
            return false;
        }
        return true;
    }

    public static UpdaterServer getInstance() {
        return instance;
    }
}