package io.github.micrafast.modupdater.server.handlers;

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

public class ModTransferHandler implements HttpRequestHandler {
    protected final Log log = LogFactory.getLog(getClass());
    ServerConfig config;

    public ModTransferHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String[] parts = request.getRequestLine().getUri().split("/");
        String fileName = parts[parts.length-1];
        File modFile = new File(this.config.modsFolder, fileName);
        response.setEntity(new FileEntity(modFile));
    }
}
