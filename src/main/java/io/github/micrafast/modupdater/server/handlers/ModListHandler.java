package io.github.micrafast.modupdater.server.handlers;

import com.google.gson.Gson;
import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.server.ServerConfig;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
import java.io.IOException;

public class ModListHandler implements HttpRequestHandler {
    final ServerConfig config;

    public ModListHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Gson gson = new Gson();
        File modDir = new File(this.config.modsFolder);
        Mod[] mods = Mod.getModList(modDir);
        String ctx = gson.toJson(mods);
        response.setEntity(new StringEntity(ctx));
    }
}
