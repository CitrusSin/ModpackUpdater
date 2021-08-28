package io.github.micrafast.modupdater.server.handlers;

import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.cfapi.CFMLink;
import io.github.micrafast.modupdater.server.ModManifestManager;
import io.github.micrafast.modupdater.server.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
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
        String md5 = parts[parts.length-1];
        Mod mod = manifestManager.getModManifest().searchMD5(md5);
        if (mod == null) {
            String fileName = URLDecoder.decode(parts[parts.length-1], "UTF-8");
            mod = manifestManager.getModManifest().searchFileName(fileName);
            if (mod == null) {
                response.setStatusCode(404);
                return;
            }
        }
        if (manifestManager.hasCurseForgeLinkService()) {
            CFMLink link = manifestManager.getCurseForge().getLink(mod);
            if (link != null) {
                // Redirect to CurseForge
                response.setStatusCode(301);
                response.setHeader("Location", link.getCurseForgeContext().getUrl());
                return;
            }
        } else {
            File modFile = mod.localFile;
            if (!modFile.exists()) {
                response.setStatusCode(404);
                return;
            }
            response.setStatusCode(200);
            response.setEntity(new FileEntity(modFile));
        }
    }
}
