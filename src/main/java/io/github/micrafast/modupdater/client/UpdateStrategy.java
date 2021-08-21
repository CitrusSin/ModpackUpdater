package io.github.micrafast.modupdater.client;

import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.network.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UpdateStrategy {
    public final ModManifest remoteManifest;
    public final List<Mod> localMods;

    public final Map<Mod, Boolean> installMods = new HashMap<>();
    public final Map<Mod, Boolean> optionalMods = new HashMap<>();
    public final Map<Mod, Boolean> removeMods = new HashMap<>();

    public final List<Thread> downloadings;
    public final Queue<Thread> queue = new LinkedList<>();

    final File modsFolder;
    final int maxThreadCount;

    Thread queueListener;

    public UpdateStrategy(ModManifest remoteManifest, File modsFolder, int maxThreadCount) {
        this.remoteManifest = remoteManifest;
        this.localMods = Mod.getModList(modsFolder);
        this.modsFolder = modsFolder;
        this.maxThreadCount = maxThreadCount;
        downloadings = new ArrayList<>();
        if (!modsFolder.exists()) {
            modsFolder.mkdirs();
        }
        this.calculateDifferences();
    }

    public void calculateDifferences() {
        for (Mod mod : remoteManifest.commonMods) {
            if (!Utils.containsMod(localMods, mod)) {
                installMods.put(mod, true);
            }
        }
        for (Mod mod : remoteManifest.optionalMods) {
            if (!Utils.containsMod(localMods, mod)) {
                optionalMods.put(mod, false);
            }
        }
        for (Mod mod : localMods) {
            if ((!Utils.containsMod(remoteManifest.commonMods, mod))
                    && (!Utils.containsMod(remoteManifest.optionalMods, mod))) {
                removeMods.put(mod, true);
            } else if (Utils.containsMod(remoteManifest.optionalMods, mod) && (!Utils.containsMod(remoteManifest.commonMods, mod))) {
                removeMods.put(mod, false);
            }
        }
    }

    public void startExecuteStrategy() {
        for (Map.Entry<Mod, Boolean> entry : removeMods.entrySet()) {
            if (entry.getValue()) {
                addQueue(new DeleteThread(entry.getKey().localFile));
            }
        }
        for (Map.Entry<Mod, Boolean> entry : installMods.entrySet()) {
            if (entry.getValue()) {
                Mod mod = entry.getKey();
                addDownloadQueue(mod, new File(modsFolder, mod.getFileName()));
            }
        }
        for (Map.Entry<Mod, Boolean> entry : optionalMods.entrySet()) {
            if (entry.getValue()) {
                Mod mod = entry.getKey();
                addDownloadQueue(mod, new File(modsFolder, mod.getFileName()));
            }
        }
        startQueueListener();
    }

    protected void addQueue(Thread t) {
        queue.add(t);
    }

    protected void addDownloadQueue(Mod mod, File file) {
        String url = remoteManifest.getRemoteUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String dURL = url + "/mods/downloads/";
        DownloadThread dt = new DownloadThread(dURL + mod.getMD5HexString(), file);
        queue.add(dt);
    }

    public boolean isRunning() {
        return (queueListener != null) && (queueListener.isAlive());
    }

    protected void startQueueListener() {
        queueListener = new Thread(() -> {
            while (true) {
                downloadings.removeIf(dt -> !dt.isAlive());
                if (queue.isEmpty() && downloadings.size() == 0) {
                    return;
                }
                if (downloadings.size() < maxThreadCount) {
                    if (!queue.isEmpty()) {
                        Thread dt = queue.poll();
                        dt.start();
                        downloadings.add(dt);
                    }
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        queueListener.start();
    }

    static class DeleteThread extends Thread {
        final File file;

        public DeleteThread(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            file.delete();
        }

        @Override
        public String toString() {
            return "${operation.delete} " + this.file.getName();
        }
    }

    static class DownloadThread extends Thread {
        public IOException exception;

        String url;
        File file;

        public DownloadThread(String url, File file) {
            this.url = url;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                NetworkUtils.download(url, file);
            } catch (IOException e) {
                this.exception = e;
            }
        }

        @Override
        public String toString() {
            return "${operation.download} " + this.file.getName();
        }
    }
}
