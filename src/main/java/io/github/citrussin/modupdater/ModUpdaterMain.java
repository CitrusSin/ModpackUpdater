package io.github.citrussin.modupdater;

import io.github.citrussin.modupdater.cli.CommandParser;
import io.github.citrussin.modupdater.cli.ModUpdaterArguments;
import io.github.citrussin.modupdater.client.UpdaterClient;
import io.github.citrussin.modupdater.server.UpdaterServer;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.CurseforgeRedirectionInitializer;
import io.github.citrussin.modupdater.server.redirection.setup.modrinth.ModrinthRedirectionInitializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class ModUpdaterMain {
    private static final Log log = LogFactory.getLog(ModUpdaterMain.class);
    public static final String SERVICE_NAME = "ModpackUpdateService";
    public static final String SERVICE_VER  = "1.4.000";

    public static void main(String[] args) {
        CommandParser<ModUpdaterArguments> parser = new CommandParser<>(ModUpdaterArguments.class);
        ModUpdaterArguments arguments;
        try {
            arguments = parser.parseCommand(args);
        } catch (Exception e) {
            log.error("Exception happened while parsing arguments", e);
            return;
        }

        // Start server or client according to arguments
        if (arguments.modrinthPackFilename != null) {
            try {
                File packFile = new File(arguments.modrinthPackFilename);
                ModrinthRedirectionInitializer configurator = new ModrinthRedirectionInitializer(packFile);
                configurator.initializeLinks();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.curseforgeConfigManifestFilename != null) {
            System.out.println("Thanks to curseforge api provided by Gaz492");
            System.out.println("API Doc: https://curseforgeapi.docs.apiary.io/");
            try {
                File manifestFile = new File(arguments.curseforgeConfigManifestFilename);
                CurseforgeRedirectionInitializer configurator = new CurseforgeRedirectionInitializer(manifestFile);
                configurator.initializeLinks();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.isServer) {
            try {
                new UpdaterServer().runServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new UpdaterClient();
        }
    }

}
