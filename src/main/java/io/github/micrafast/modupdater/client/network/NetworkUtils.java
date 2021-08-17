package io.github.micrafast.modupdater.client.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NetworkUtils {
    private static Log log = LogFactory.getLog(NetworkUtils.class);
    static CloseableHttpClient httpClient;

    static {
        httpClient = HttpClients.createDefault();
        try {
            SSLContext ctx = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
            CloseableHttpClient prevClient = httpClient;
            httpClient = HttpClients.custom()
                    .setSSLContext(ctx)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            prevClient.close();
        } catch (Exception e) {
            log.error("SSL Certification load failed", e);
        }
    }

    public static String getString(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        String result = httpClient.execute(get, new StringResponseHandler());
        return result;
    }

    public static void download(String url, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        HttpGet get = new HttpGet(url);
        FileOutputStream stream = new FileOutputStream(file, false);
        httpClient.execute(get, new DownloadResponseHandler(stream));
        stream.close();
    }

    public static void closeClient() throws IOException {
        httpClient.close();
    }

    static class StringResponseHandler implements ResponseHandler<String> {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    }

    static class DownloadResponseHandler implements ResponseHandler<Void> {
        private OutputStream stream;

        public DownloadResponseHandler(OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public Void handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                entity.writeTo(stream);
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            return null;
        }
    }
}
