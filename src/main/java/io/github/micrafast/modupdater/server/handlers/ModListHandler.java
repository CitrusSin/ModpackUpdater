package io.github.micrafast.modupdater.server.handlers;

import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.server.ServerConfig;
import io.github.micrafast.modupdater.server.UpdaterServer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ModListHandler implements HttpRequestHandler {
    final ServerConfig config;

    public ModListHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        ModManifest mods = UpdaterServer.getInstance().getManifestManager().getModManifest();
        String ctx = mods.getJson();
        response.setStatusCode(200);
        ByteArrayEntity entity = new ByteArrayEntity(ctx.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON);
        entity.setContentEncoding("UTF-8");
        response.setEntity(entity);
    }
}
