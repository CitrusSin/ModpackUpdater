package io.github.citrussin.modupdater.client;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.ModManifest;
import io.github.citrussin.modupdater.async.AsyncTaskQueueRunner;
import io.github.citrussin.modupdater.async.AsyncTaskQueueRunnerBuilder;
import io.github.citrussin.modupdater.network.TaskDelete;
import io.github.citrussin.modupdater.network.TaskDownload;
import io.github.citrussin.modupdater.network.TaskFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateStrategy {
    public final ModManifest remoteManifest;

    private Map<Mod, Boolean> installMods = new HashMap<>();
    private Map<Mod, Boolean> optionalMods = new HashMap<>();
    private Map<Mod, Boolean> removeMods = new HashMap<>();

    private final Log log = LogFactory.getLog(getClass());

    //public final List<Thread> downloadings;
    //public final Queue<Thread> queue = new LinkedList<>();

    final File modsFolder;
    final int maxThreadCount;

    //Thread queueListener;

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

    public Map<Mod, Boolean> getInstallMods() {
        return installMods;
    }

    public Map<Mod, Boolean> getOptionalMods() {
        return optionalMods;
    }

    public Map<Mod, Boolean> getRemoveMods() {
        return removeMods;
    }

    public void calculateDifferences() {
        List<Mod> localMods = Mod.getModList(modsFolder);
        installMods.clear();
        optionalMods.clear();
        removeMods.clear();
        for (Mod mod : remoteManifest.commonMods) {
            if (!localMods.contains(mod)) {
                installMods.put(mod, true);
            }
        }
        for (Mod mod : remoteManifest.optionalMods) {
            if (!localMods.contains(mod)) {
                optionalMods.put(mod, false);
            }
        }
        for (Mod mod : localMods) {
            if ((!remoteManifest.commonMods.contains(mod))
                    && (!remoteManifest.optionalMods.contains(mod))) {
                removeMods.put(mod, true);
            } else if (remoteManifest.optionalMods.contains(mod) && !remoteManifest.commonMods.contains(mod)) {
                removeMods.put(mod, false);
            }
        }
    }

    public AsyncTaskQueueRunner<TaskFileOperation, String> getTaskRunner() {
        AsyncTaskQueueRunnerBuilder<TaskFileOperation, String> builder = new AsyncTaskQueueRunnerBuilder<>();
        for (Map.Entry<Mod, Boolean> entry : removeMods.entrySet()) {
            if (entry.getValue()) {
                builder.addTask(new TaskDelete(entry.getKey().localFile));
                //addQueue(new DeleteThread(entry.getKey().localFile));
            }
        }
        for (Map.Entry<Mod, Boolean> entry : installMods.entrySet()) {
            if (entry.getValue()) {
                Mod mod = entry.getKey();
                addDownloadQueue(builder, mod, new File(modsFolder, mod.getFilename()));
            }
        }
        for (Map.Entry<Mod, Boolean> entry : optionalMods.entrySet()) {
            if (entry.getValue()) {
                Mod mod = entry.getKey();
                addDownloadQueue(builder, mod, new File(modsFolder, mod.getFilename()));
            }
        }
        return builder.build();
    }


    protected void addDownloadQueue(AsyncTaskQueueRunnerBuilder<TaskFileOperation, String> builder, Mod mod, File file) {
        String url = remoteManifest.getRemoteUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String dURL = url + "/mods/downloads/";
        TaskDownload dt = new TaskDownload(dURL + mod.getMD5HexString(), file);
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
}
