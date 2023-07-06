package io.github.citrussin.modupdater.server.redirection.setup.curseforge;

import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.async.TaskQueueBuilder;
import io.github.citrussin.modupdater.server.redirection.setup.PackInitializer;
import io.github.citrussin.modupdater.server.redirection.setup.TaskDownloadSourceMod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class CurseforgePackInitializer extends PackInitializer {
    private CurseforgeManifest manifest;

    private static final Log log = LogFactory.getLog(CurseforgePackInitializer.class);

    public CurseforgePackInitializer(File jsonFile) throws IOException {
        super();
        String json = Utils.readFile(jsonFile);
        manifest = CurseforgeManifest.fromJson(json);
    }

    @Override
    protected void initializeDownloadTasks(TaskQueueBuilder<TaskDownloadSourceMod, String> tasksRunnerBuilder) {
        File modsFolder = new File(serverConfig.commonModsFolder);
        for (CurseforgeMod mod : manifest.files) {
            tasksRunnerBuilder.addTask(new TaskDownloadSourceMod(mod.getUrl(), new File(modsFolder, mod.getName())));
            log.info(String.format("%s added to download list", mod.getName()));
        }
    }
}