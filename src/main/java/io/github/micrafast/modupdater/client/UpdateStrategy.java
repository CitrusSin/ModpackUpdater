package io.github.micrafast.modupdater.client;

import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.async.AsyncTaskQueueRunner;
import io.github.micrafast.modupdater.async.AsyncTaskQueueRunnerBuilder;
import io.github.micrafast.modupdater.async.Task;
import io.github.micrafast.modupdater.network.NetworkUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateStrategy {
    public final ModManifest remoteManifest;

    public Map<Mod, Boolean> getInstallMods() {
        return installMods;
    }

    public Map<Mod, Boolean> getOptionalMods() {
        return optionalMods;
    }

    public Map<Mod, Boolean> getRemoveMods() {
        return removeMods;
    }

    public boolean isRunning() {
        return (queueListener != null) && (queueListener.isAlive());
    }

    private Map<Mod, Boolean> installMods = new HashMap<>();
    private Map<Mod, Boolean> optionalMods = new HashMap<>();
    private Map<Mod, Boolean> removeMods = new HashMap<>();

    private final Log log = LogFactory.getLog(getClass());

    //public final List<Thread> downloadings;
    //public final Queue<Thread> queue = new LinkedList<>();

    final File modsFolder;
    final int maxThreadCount;

    Thread queueListener;

    public UpdateStrategy(ModManifest remoteManifest, File modsFolder, int maxThreadCount) {
        this.remoteManifest = remoteManifest;
        if (!modsFolder.exists()) {
            boolean result = modsFolder.mkdirs();
            if (!result) {
                log.error("mkdir failed");
            }
        }
        this.modsFolder = modsFolder;
        this.maxThreadCount = maxThreadCount;
        //downloadings = new ArrayList<>();
        this.calculateDifferences();
    }

    public void calculateDifferences() {
        List<Mod> localMods = Mod.getModList(modsFolder);
        installMods.clear();
        optionalMods.clear();
        removeMods.clear();
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

    public AsyncTaskQueueRunner<Task<String,? extends Exception>, String, Exception> getTaskRunner() {
        AsyncTaskQueueRunnerBuilder<Task<String,? extends Exception>,String,Exception> builder = new AsyncTaskQueueRunnerBuilder<>();
        for (Map.Entry<Mod, Boolean> entry : removeMods.entrySet()) {
            if (entry.getValue()) {
                builder.addTask(new DeleteThread(entry.getKey().localFile));
                //addQueue(new DeleteThread(entry.getKey().localFile));
            }
        }
        for (Map.Entry<Mod, Boolean> entry : installMods.entrySet()) {
            if (entry.getValue()) {
                Mod mod = entry.getKey();
                addDownloadQueue(builder, mod, new File(modsFolder, mod.getFileName()));
            }
        }
        for (Map.Entry<Mod, Boolean> entry : optionalMods.entrySet()) {
            if (entry.getValue()) {
                Mod mod = entry.getKey();
                addDownloadQueue(builder, mod, new File(modsFolder, mod.getFileName()));
            }
        }
        return builder.build();
    }


    protected void addDownloadQueue(AsyncTaskQueueRunnerBuilder<Task<String, ? extends Exception>,?,?> builder, Mod mod, File file) {
        String url = remoteManifest.getRemoteUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String dURL = url + "/mods/downloads/";
        DownloadThread dt = new DownloadThread(dURL + mod.getMD5HexString(), file);
        builder.addTask(dt);
    }

    /*
    protected void addQueue(Thread t) {
        queue.add(t);
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
    */

    static class DeleteThread extends Task<String, Exception> {
        public boolean result;
        final File file;

        public DeleteThread(File file) {
            this.file = file;
        }

        @Override
        public void execute() throws Exception {
            result = file.delete();
        }

        @Override
        public String toString() {
            return "${operation.delete} " + this.file.getName();
        }
    }

    static class DownloadThread extends Task<String, IOException> {
        String url;
        File file;

        public DownloadThread(String url, File file) {
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
            return "${operation.download} " + this.file.getName();
        }
    }
}
