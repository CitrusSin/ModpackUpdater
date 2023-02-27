package io.github.citrussin.modupdater.network;

import io.github.citrussin.modupdater.async.Task;

import java.io.File;

public abstract class TaskFileOperation extends Task<String> {
    protected File file;

    public File getFile() {
        return file;
    }
}
