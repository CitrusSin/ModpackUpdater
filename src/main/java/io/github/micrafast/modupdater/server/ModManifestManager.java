package io.github.micrafast.modupdater.server;

import io.github.micrafast.modupdater.ModManifest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Date;

import static java.nio.file.StandardWatchEventKinds.*;

public class ModManifestManager {
    private static final long MOD_UPDATE_INTERVAL = 1200000;
    private static final long WATCH_SERVICE_DELAY = 1000;

    private final Log log = LogFactory.getLog(getClass());

    protected boolean watchMode = true;
    protected ServerConfig config;
    protected ModManifest modManifest;
    protected Date lastUpdateDate;
    WatchService watchService;

    public ModManifestManager(ServerConfig config) {
        this.config = config;
        try {
            watchService = FileSystems.getDefault().newWatchService();

            Path    dir1 = Paths.get(this.config.commonModsFolder),
                    dir2 = Paths.get(this.config.optionalModsFolder);
            dir1.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            dir2.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            Thread watchThread = new Thread(new WatcherRunnable());
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (IOException e) {
            watchMode = false;
            log.error(e);
        }
        update();
    }

    protected void update() {
        log.info("Mod directory updated.");
        modManifest = new ModManifest(this.config.commonModsFolder, this.config.optionalModsFolder);
        lastUpdateDate = new Date();
    }

    public ModManifest getModManifest() {
        if (!watchMode) {
            Date now = new Date();
            if (now.getTime() - lastUpdateDate.getTime() > MOD_UPDATE_INTERVAL) {
                update();
            }
        }
        return modManifest;
    }

    public void close() {
        try {
            watchService.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    class WatcherRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    return;
                }
                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }
                    ModManifestManager.this.update();
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }
}
