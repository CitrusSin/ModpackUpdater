package io.github.citrussin.modupdater.network;

import java.io.File;
import java.io.IOException;

public class TaskDelete extends TaskFileOperation {
    final File file;

    public TaskDelete(File file) {
        this.file = file;
    }

    @Override
    public void execute() throws IOException {
        boolean result = file.delete();
        if (!result) {
            throw new IOException("Failed to download " + this.file.getName());
        }
    }

    @Override
    public String toString() {
        return "${operation.delete} " + this.file.getName() + " " + this.getProgress();
    }
}
