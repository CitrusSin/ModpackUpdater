package io.github.citrussin.modupdater.server.handlers;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.server.ModManifestManager;
import io.github.citrussin.modupdater.server.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.net.URLDecoder;

public class ModTransferHandler implements HttpRequestHandler {
    protected final Log log = LogFactory.getLog(getClass());
    ServerConfig config;
    ModManifestManager manifestManager;

    public ModTransferHandler(ServerConfig config, ModManifestManager manifestManager) {
        this.config = config;
        this.manifestManager = manifestManager;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String[] parts = request.getRequestLine().getUri().split("/");
        String md5 = parts[parts.length - 1];
        Mod mod = manifestManager.getModManifest().searchMD5(md5);
        if (mod == null) {
            String fileName = URLDecoder.decode(parts[parts.length - 1], "UTF-8");
            mod = manifestManager.getModManifest().searchFilename(fileName);
            if (mod == null) {
                response.setStatusCode(404);
                return;
            }
        }
        manifestManager.getModManifest().getProvider(mod).handleResponse(request, response, context);
    }
}
