package io.github.citrussin.modupdater.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NetworkUtils {
    private static final Log log = LogFactory.getLog(NetworkUtils.class);
    static CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager poolMan = new PoolingHttpClientConnectionManager();
        poolMan.setMaxTotal(200);
        poolMan.setDefaultMaxPerRoute(50);
        httpClient = HttpClients.custom().setConnectionManager(poolMan).build();
    }

    public static String getString(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        return httpClient.execute(get, new StringResponseHandler());
    }

    public static String getStringWithUserAgent(String url, String userAgent) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", userAgent);
        return httpClient.execute(get, new StringResponseHandler());
    }

    public static void download(String url, File file) throws IOException {
        if (!file.exists()) {
            boolean res = file.createNewFile();
            if (!res) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }
        FileOutputStream stream = new FileOutputStream(file, false);
        downloadToStream(stream, url);
        stream.close();
    }

    public static void downloadToStream(OutputStream stream, String url) throws IOException {
        HttpGet get = new HttpGet(url);
        httpClient.execute(get, new DownloadResponseHandler(stream));
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
