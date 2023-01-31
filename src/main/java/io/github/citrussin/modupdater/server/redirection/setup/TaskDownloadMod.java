package io.github.citrussin.modupdater.server.redirection.setup;

import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.network.TaskDownload;
import io.github.citrussin.modupdater.server.redirection.ModRedirection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TaskDownloadMod extends TaskDownload {
    private final Log log = LogFactory.getLog(getClass());

    public ModRedirection redirection = null;

    public TaskDownloadMod(String url, File file) {
        super(url, file);
    }

    @Override
    protected void execute() throws IOException {
        super.execute();
        try {
            this.redirection = new ModRedirection(Utils.calculateFileHash(this.file), this.url);
        } catch (NoSuchAlgorithmException e) {
            log.error("Calculate MD5 failed", e);
        }
    }
}
