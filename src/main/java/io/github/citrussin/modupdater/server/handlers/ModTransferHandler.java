package io.github.citrussin.modupdater.server.handlers;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.server.ModManifestManager;
import io.github.citrussin.modupdater.server.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ModTransferHandler implements HttpRequestHandler {
    protected final Log log = LogFactory.getLog(getClass());
    ServerConfig config;
    ModManifestManager manifestManager;

    public ModTransferHandler(ServerConfig config, ModManifestManager manifestManager) {
        this.config = config;
        this.manifestManager = manifestManager;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
        String uri = request.getRequestLine().getUri();
        if (uri.endsWith("/")) {
            response.setStatusCode(404);
            return;
        }
        String requestName = uri.substring(uri.lastIndexOf('/')+1);
        Mod mod = manifestManager.getModManifest().searchByHash(requestName);
        if (mod == null) {  // if request name is filename instead of md5
            String fileName = URLDecoder.decode(requestName, StandardCharsets.UTF_8.name());
            mod = manifestManager.getModManifest().searchFilename(fileName);
            if (mod == null) {
                response.setStatusCode(404);
                return;
            }
        }
        manifestManager.getModManifest().getProvider(mod).handleResponse(request, response, context);
    }
}
