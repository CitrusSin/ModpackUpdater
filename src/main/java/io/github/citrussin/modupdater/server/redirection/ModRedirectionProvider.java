package io.github.citrussin.modupdater.server.redirection;

import com.google.gson.annotations.Expose;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import java.util.Map;

public class ModRedirectionProvider implements ModProvider {
    @Expose
    public Map<String, String> hashValues;

    @Expose
    public String url;

    public ModRedirectionProvider() {}

    public ModRedirectionProvider(Map<String, String> hashValues, String url) {
        this.hashValues = hashValues;
        this.url = url;
    }

    public void handleResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        // 301 Moved Permanently
        response.setStatusCode(301);
        response.setHeader("Location", url);
    }
}
