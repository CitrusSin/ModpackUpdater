package io.github.micrafast.modupdater.server;

import io.github.micrafast.modupdater.ModManifest;

import java.util.Date;

public class ModManifestManager {
    private static final long MOD_UPDATE_INTERVAL = 1200000;

    protected ServerConfig config;
    protected ModManifest modManifest;
    protected Date lastUpdateDate;

    public ModManifestManager(ServerConfig config) {
        this.config = config;
        update();
    }

    protected void update() {
        modManifest = new ModManifest(this.config.commonModsFolder, this.config.optionalModsFolder);
        lastUpdateDate = new Date();
    }

    public ModManifest getModManifest() {
        Date now = new Date();
        if (now.getTime()-lastUpdateDate.getTime() > MOD_UPDATE_INTERVAL) {
            update();
        }
        return modManifest;
    }

}
