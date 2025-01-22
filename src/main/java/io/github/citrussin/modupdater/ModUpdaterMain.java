package io.github.citrussin.modupdater;

import io.github.citrussin.modupdater.cli.CommandParser;
import io.github.citrussin.modupdater.cli.ModUpdaterArguments;
import io.github.citrussin.modupdater.client.Client;
import io.github.citrussin.modupdater.server.Server;
import io.github.citrussin.modupdater.server.redirection.setup.curseforge.CurseforgePackInitializer;
import io.github.citrussin.modupdater.server.redirection.setup.modrinth.ModrinthPackInitializer;
import io.github.citrussin.modupdater.server.redirection.setup.modrinth.ModrinthRedirectionInitalizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class ModUpdaterMain {
    private static final Log log = LogFactory.getLog(ModUpdaterMain.class);
    public static final String SERVICE_NAME = "ModpackUpdateService";
    public static final String SERVICE_VER  = "1.5.001";

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
        if (arguments.modrinthParse) {
            try {
                ModrinthRedirectionInitalizer redirectionInitalizer = new ModrinthRedirectionInitalizer();
                redirectionInitalizer.initializeRedirections();
                System.exit(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (arguments.modrinthPackFilename != null) {
            try {
                File packFile = new File(arguments.modrinthPackFilename);
                ModrinthPackInitializer configurator = new ModrinthPackInitializer(packFile);
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
                CurseforgePackInitializer configurator = new CurseforgePackInitializer(manifestFile);
                configurator.initializeLinks();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.isServer) {
            try {
                new Server().runServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new Client();
        }
    }

}
