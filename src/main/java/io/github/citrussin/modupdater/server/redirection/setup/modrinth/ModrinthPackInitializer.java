package io.github.citrussin.modupdater.server.redirection.setup.modrinth;

import io.github.citrussin.modupdater.async.TaskQueueBuilder;
import io.github.citrussin.modupdater.server.redirection.setup.PackInitializer;
import io.github.citrussin.modupdater.server.redirection.setup.TaskDownloadSourceMod;

import java.io.File;
import java.io.IOException;

public class ModrinthPackInitializer extends PackInitializer {
    private ModrinthPack modrinthPack;

    public ModrinthPackInitializer(File packFile) throws IOException {
        super();
        modrinthPack = ModrinthPack.fromMrPack(packFile);
    }

    @Override
    protected void initializeDownloadTasks(TaskQueueBuilder<TaskDownloadSourceMod, String> tasksRunnerBuilder) {
        File commonModsFolder = new File(serverConfig.commonModsFolder);
        File optionalModsFolder = new File(serverConfig.optionalModsFolder);

        for (ModrinthFile file: modrinthPack.files) {
            File destinationFolder;
            if (file.env.server.equals("unsupported")) {
                destinationFolder = optionalModsFolder;
            } else {
                destinationFolder = commonModsFolder;
            }

            String name = file.getName();
            TaskDownloadSourceMod task = new TaskDownloadSourceMod(file.downloads.get(0), new File(destinationFolder, name));
            tasksRunnerBuilder.addTask(task);
            System.out.printf("%s added to download task%n", name);
        }
    }
}
