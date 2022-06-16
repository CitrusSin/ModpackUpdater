package io.github.citrussin.modupdater.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;

import java.io.IOException;

public class WorkerThread extends Thread {
    protected final Log log = LogFactory.getLog(getClass());
    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public WorkerThread(
            final HttpService httpservice,
            final HttpServerConnection conn) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
    }

    @Override
    public void run() {
        HttpContext context = new BasicHttpContext(null);
        try {
            while (!Thread.interrupted() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
            }
        } catch (ConnectionClosedException ex) {
            // Shut up
        } catch (IOException ex) {
            // Shut up
        } catch (HttpException ex) {
            log.error("Unrecoverable HTTP protocol violation: ", ex);
        } finally {
            try {
                this.conn.shutdown();
            } catch (IOException ignore) {}
        }
    }
}
