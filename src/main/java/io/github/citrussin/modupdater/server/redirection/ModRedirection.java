package io.github.citrussin.modupdater.server.redirection;

import com.google.gson.annotations.Expose;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class ModRedirection implements ModProvider {
    @Expose
    public String md5;

    @Expose
    public String url;

    public ModRedirection() {}

    public ModRedirection(String md5, String url) {
        this.md5 = md5;
        this.url = url;
    }

    public void handleResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        // 301 Moved Permanently
        response.setStatusCode(301);
        response.setHeader("Location", url);
    }
}
