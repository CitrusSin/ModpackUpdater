package io.github.citrussin.modupdater.server.redirection;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public interface ModProvider {
    void handleResponse(HttpRequest request, HttpResponse response, HttpContext context);
}
