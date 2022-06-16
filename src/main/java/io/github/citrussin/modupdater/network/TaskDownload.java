package io.github.citrussin.modupdater.network;

import java.io.File;
import java.io.IOException;

public class TaskDownload extends TaskFileOperation {
    protected String url;
    protected File file;

    public TaskDownload(String url, File file) {
        this.url = url;
        this.file = file;
    }

    @Override
    protected void execute() throws IOException {
        this.setProgress("Downloading");
        NetworkUtils.download(url, file);
        this.setProgress("Completed");
    }

    @Override
    public String toString() {
        return "${operation.download} " + this.file.getName() + " " + this.getProgress();
    }
}
