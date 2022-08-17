package io.github.citrussin.modupdater.server.redirection.setup.curseforge;

import io.github.citrussin.modupdater.ModManifest;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.async.TaskQueueBuilder;
import io.github.citrussin.modupdater.server.redirection.setup.RedirectionInitializer;
import io.github.citrussin.modupdater.server.redirection.setup.TaskDownloadMod;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.api.CurseforgeManifest;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.api.CurseforgeMod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class CurseforgeRedirectionInitializer extends RedirectionInitializer {
    private CurseforgeManifest manifest;

    private static final Log log = LogFactory.getLog(CurseforgeRedirectionInitializer.class);

    public CurseforgeRedirectionInitializer(File jsonFile) throws IOException {
        super();
        String json = Utils.readFile(jsonFile, "UTF-8");
        manifest = CurseforgeManifest.fromJson(json);
    }

    @Override
    protected void initializeDownloadTasks(ModManifest localManifest, TaskQueueBuilder<TaskDownloadMod, String> tasksRunnerBuilder) {
        File modsFolder = new File(serverConfig.commonModsFolder);
        for (CurseforgeMod mod : manifest.files) {
            tasksRunnerBuilder.addTask(new TaskDownloadMod(mod.getUrl(), new File(modsFolder, mod.getName())));
            log.info(String.format("%s added to download list", mod.getName()));
        }
    }
}