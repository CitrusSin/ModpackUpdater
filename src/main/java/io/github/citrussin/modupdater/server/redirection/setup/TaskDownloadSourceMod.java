package io.github.citrussin.modupdater.server.redirection.setup;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.ModManifest;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.network.TaskDownload;
import io.github.citrussin.modupdater.server.redirection.ModRedirectionProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TaskDownloadSourceMod extends TaskDownload {
    private final Log log = LogFactory.getLog(getClass());

    // Output field
    public ModRedirectionProvider redirection = null;

    public TaskDownloadSourceMod(String url, File file) {
        super(url, file);
    }

    @Override
    protected void execute() throws IOException {
        super.execute();
        this.redirection = new ModRedirectionProvider(Utils.calculateFileHash(this.file, Mod.HASH_ALGORITHMS), this.url);
    }
}
