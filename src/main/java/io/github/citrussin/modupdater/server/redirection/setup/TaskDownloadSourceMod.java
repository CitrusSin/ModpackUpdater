package io.github.citrussin.modupdater.server.redirection.setup;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.network.TaskDownload;
import io.github.citrussin.modupdater.server.redirection.ModRedirectionProvider;

import java.io.File;
import java.io.IOException;

public class TaskDownloadSourceMod extends TaskDownload {

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
