package io.github.citrussin.modupdater.server.redirection;

import io.github.citrussin.modupdater.Mod;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;

import java.io.File;

public class ModUpload implements ModProvider {
    private File localFile;

    public ModUpload(Mod mod) {
        this.localFile = mod.localFile;
    }

    @Override
    public void handleResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (localFile != null && localFile.exists()) {
            // 200 OK
            response.setStatusCode(200);
            response.setHeader("Content-Type", "application/java-archive");
            response.setEntity(new FileEntity(localFile));
        } else {
            // 404 Not Found
            response.setStatusCode(404);
        }
    }
}
