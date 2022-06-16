package io.github.citrussin.modupdater.server.redirection.setup.curseforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.ModManifest;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.async.AsyncTaskQueueRunner;
import io.github.citrussin.modupdater.async.AsyncTaskQueueRunnerBuilder;
import io.github.citrussin.modupdater.network.TaskDownload;
import io.github.citrussin.modupdater.server.ServerConfig;
import io.github.citrussin.modupdater.server.UpdaterServer;
import io.github.citrussin.modupdater.server.redirection.ModRedirection;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.api.CurseforgeManifest;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.api.CurseforgeMod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class CurseforgeRedirectionInitializer {
    private CurseforgeManifest manifest;
    private ServerConfig serverConfig;

    private final Gson prettyGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    private static final Log log = LogFactory.getLog(CurseforgeRedirectionInitializer.class);

    public CurseforgeRedirectionInitializer(File jsonFile) throws IOException {
        String json = Utils.readFile(jsonFile, "UTF-8");
        manifest = CurseforgeManifest.fromJson(json);

        // Load configuration file
        serverConfig = new ServerConfig();
        File serverConfigFile = new File(UpdaterServer.CONFIG_FILE_NAME);
        if (!serverConfigFile.exists()) {
            Utils.writeFile(serverConfigFile, "UTF-8", prettyGson.toJson(serverConfig));
        } else {
            serverConfig = prettyGson.fromJson(Utils.readFile(serverConfigFile,"UTF-8"), ServerConfig.class);
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

    public void initializeNewLink() {
        // Prepare variables
        List<ModRedirection> list = new LinkedList<>();
        ModManifest localManifest = new ModManifest(serverConfig.commonModsFolder, serverConfig.optionalModsFolder);
        File modsFolder = new File(serverConfig.commonModsFolder);
        AsyncTaskQueueRunnerBuilder<TaskDownloadMod, String> tasksRunnerBuilder = new AsyncTaskQueueRunnerBuilder<>();
        tasksRunnerBuilder.setMaxThreadCount(this.serverConfig.maxThreadCount);
        // Initializing download task
        for (CurseforgeMod mod : manifest.files) {
            Mod localMod = localManifest.searchFilename(mod.getName());
            if (localMod != null) {
                list.add(new ModRedirection(localMod.getMD5HexString(), mod.getUrl()));
                log.info(String.format("%s already exists, created redirection", mod.getName()));
            } else {
                tasksRunnerBuilder.addTask(new TaskDownloadMod(mod, new File(modsFolder, mod.getName())));
                log.info(String.format("%s does not exist, added it to download list", mod.getName()));
            }
        }
        // Set up tasks runner, register progress bar updating procedure
        AsyncTaskQueueRunner<TaskDownloadMod, String> tasksRunner = tasksRunnerBuilder.build();
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
        String json = prettyGson.toJson(list);
        File redirectionListFile = new File(this.serverConfig.redirectionListPath);
        try {
            Utils.writeFile(redirectionListFile, "UTF-8", json);
        } catch (IOException e) {
            log.error("Failed to write redirection list file " + redirectionListFile.getName(), e);
        }
    }

    static class TaskDownloadMod extends TaskDownload {
        public ModRedirection redirection = null;

        public TaskDownloadMod(CurseforgeMod mod, File file) {
            super(mod.getUrl(), file);
        }

        @Override
        protected void execute() throws IOException {
            super.execute();
            try {
                this.redirection = new ModRedirection(Utils.calculateMD5(this.file), this.url);
            } catch (NoSuchAlgorithmException e) {
                log.error("Calculate MD5 failed", e);
            }
        }
    }
}