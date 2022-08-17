package io.github.citrussin.modupdater.server.redirection.setup.modrinth.api;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

public class ModrinthFile {
    @Expose
    public String path;

    @Expose
    public Map<String, String> hashes;

    @Expose
    public ModrinthEnv env;

    @Expose
    public List<String> downloads;

    @Expose
    public int fileSize;

    public String getName() {
        if (path.length() > 0) {
            String[] splits = path.split("/");
            return splits[splits.length - 1];
        } else {
            return null;
        }
    }
}
