package io.github.citrussin.modupdater.server.handlers;

import io.github.citrussin.modupdater.HashAlgorithm;
import io.github.citrussin.modupdater.server.ModManifestManager;
import io.github.citrussin.modupdater.server.ServerConfig;
import io.github.citrussin.modupdater.server.redirection.ModProvider;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.security.MessageDigest;

public class ModHashTransferHandler implements HttpRequestHandler {

    private ServerConfig config;
    private ModManifestManager manifestManager;
    private HashAlgorithm hashAlgorithm;

    public ModHashTransferHandler(ServerConfig config, ModManifestManager manifestManager, HashAlgorithm hashAlgorithm) {
        this.config = config;
        this.manifestManager = manifestManager;
        this.hashAlgorithm = hashAlgorithm;
    }
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String uri = request.getRequestLine().getUri();
        if (uri.endsWith("/")) {
            response.setStatusCode(404);
            return;
        }
        String requestName = uri.substring(uri.lastIndexOf('/') + 1);
        ModProvider modProvider = manifestManager.getModManifest().getProviderByHash(hashAlgorithm, requestName);
        if (modProvider == null) {
            response.setStatusCode(404);
            return;
        }
        modProvider.handleResponse(request, response, context);
    }
}
