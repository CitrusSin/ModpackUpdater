package io.github.micrafast.modupdater.server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.server.ServerConfig;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ModListHandler implements HttpRequestHandler {
    final ServerConfig config;

    public ModListHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        File commonModDir = new File(this.config.commonModsFolder);
        File optionalModDir = new File(this.config.optionalModsFolder);
        ModManifest mods = new ModManifest(commonModDir, optionalModDir);
        String ctx = gson.toJson(mods);
        response.setStatusCode(200);
        ByteArrayEntity entity = new ByteArrayEntity(ctx.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON);
        entity.setContentEncoding("UTF-8");
        response.setEntity(entity);
    }
}
