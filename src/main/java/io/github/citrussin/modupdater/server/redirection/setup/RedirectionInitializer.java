package io.github.citrussin.modupdater.server.redirection.setup;

import io.github.citrussin.modupdater.GsonManager;
import io.github.citrussin.modupdater.ModManifest;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.async.TaskQueue;
import io.github.citrussin.modupdater.async.TaskQueueBuilder;
import io.github.citrussin.modupdater.server.ServerConfig;
import io.github.citrussin.modupdater.server.UpdaterServer;
import io.github.citrussin.modupdater.server.redirection.ModRedirection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class RedirectionInitializer {
    private static final Log log = LogFactory.getLog(RedirectionInitializer.class);

    protected ServerConfig serverConfig;

    public RedirectionInitializer() throws IOException {
        // Load configuration file
        serverConfig = new ServerConfig();
        File serverConfigFile = new File(UpdaterServer.CONFIG_FILE_NAME);
        if (!serverConfigFile.exists()) {
            Utils.writeFile(serverConfigFile, "UTF-8", GsonManager.prettyGson.toJson(serverConfig));
        } else {
            serverConfig = GsonManager.prettyGsonExcludeWithoutExpose
                    .fromJson(Utils.readFile(serverConfigFile,"UTF-8"), ServerConfig.class);
        }

        // Create mods folder
        File    dir1 = new File(serverConfig.commonModsFolder),
                dir2 = new File(serverConfig.optionalModsFolder);
        if (!dir1.isDirectory()) {
            dir1.mkdirs();
        }
        if (!dir2.isDirectory()) {
            dir2.mkdirs();
        }
    }

    private int percent = 0;
    private final int progressBarLen = 65;

    private String repeatChar(char raw, int count) {
        StringBuilder repeatBuilder = new StringBuilder();
        for (int i=0;i<count;i++) {
            repeatBuilder.append(raw);
        }
        return repeatBuilder.toString();
    }

    private void outputProgress(int percent) {
        synchronized (log) {
            this.percent = percent;
            String progressTips = "Progress: ";
            int aChars = percent * (progressBarLen - progressTips.length() - 5) / 100;
            int bChars = (progressBarLen - progressTips.length() - 5) - aChars;
            String lastNum = percent + "%";
            System.out.print(repeatChar('\b', progressBarLen));
            String progress = progressTips +
                    '[' +
                    repeatChar('#', aChars) +
                    repeatChar(' ', bChars) +
                    ']' +
                    lastNum;
            System.out.print(progress);
        }
    }

    protected abstract void initializeDownloadTasks(ModManifest localManifest, TaskQueueBuilder<TaskDownloadMod, String> tasksRunnerBuilder);

    public void initializeLinks() {
        // Prepare variables
        List<ModRedirection> list = new LinkedList<>();
        ModManifest localManifest = new ModManifest(serverConfig.commonModsFolder, serverConfig.optionalModsFolder);
        TaskQueueBuilder<TaskDownloadMod, String> tasksRunnerBuilder = new TaskQueueBuilder<>();
        tasksRunnerBuilder.setMaxThreadCount(this.serverConfig.maxThreadCount);
        // Initializing download task
        initializeDownloadTasks(localManifest, tasksRunnerBuilder);
        // Set up tasks runner, register progress bar updating procedure
        TaskQueue<TaskDownloadMod, String> tasksRunner = tasksRunnerBuilder.build();
        tasksRunner.addWatchCallback((tr) -> outputProgress((int)tr.getPercent()));
        tasksRunner.addExceptionCallback((task, ex) -> {
            System.out.print(repeatChar('\b', progressBarLen));
            log.error("A task ran into an exception.", ex);
            outputProgress(this.percent);
        });
        // Download!
        log.info("Downloading, please wait...");
        tasksRunner.runTaskQueueWithThreadBlock();
        System.out.println();
        // After download, finish the rest of the redirection list
        tasksRunner.forEachFinished((task) -> {
            if (task.redirection != null) {
                list.add(task.redirection);
            }
        });
        // Write redirection file
        String json = GsonManager.prettyGsonExcludeWithoutExpose.toJson(list);
        File redirectionListFile = new File(this.serverConfig.redirectionListPath);
        try {
            Utils.writeFile(redirectionListFile, "UTF-8", json);
        } catch (IOException e) {
            log.error("Failed to write redirection list file " + redirectionListFile.getName(), e);
        }
    }
}
