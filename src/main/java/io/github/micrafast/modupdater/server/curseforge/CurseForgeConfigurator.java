package io.github.micrafast.modupdater.server.curseforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.ModManifest;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.cfapi.CFMLink;
import io.github.micrafast.modupdater.cfapi.CurseForgeManifest;
import io.github.micrafast.modupdater.cfapi.CurseForgeMod;
import io.github.micrafast.modupdater.server.ServerConfig;
import io.github.micrafast.modupdater.server.UpdaterServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CurseForgeConfigurator {
    private CurseForgeManifest manifest;
    private ServerConfig serverConfig;

    private final Gson prettyGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    private static final Log log = LogFactory.getLog(CurseForgeConfigurator.class);

    public CurseForgeConfigurator(File jsonFile) throws IOException {
        String json = Utils.readFile(jsonFile, "UTF-8");
        manifest = CurseForgeManifest.fromJson(json);

        serverConfig = new ServerConfig();
        File serverConfigFile = new File(UpdaterServer.CONFIG_FILE_NAME);
        if (!serverConfigFile.exists()) {
            Utils.writeFile(serverConfigFile, "UTF-8", prettyGson.toJson(serverConfig));
        } else {
            serverConfig = prettyGson.fromJson(Utils.readFile(serverConfigFile,"UTF-8"), ServerConfig.class);
        }

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

    public void initializeNewLink(){
        Queue<DownloadThread> downloadWaitings = new LinkedList<>();
        Set<DownloadThread> downloadings = new HashSet<>();
        ModManifest localManifest = new ModManifest(serverConfig.commonModsFolder, serverConfig.optionalModsFolder);
        List<CFMLink> links = new ArrayList<>(manifest.files.size());
        File modsFolder = new File(serverConfig.commonModsFolder);
        for (CurseForgeMod mod : manifest.files) {
            try {
                if (localManifest.searchFileName(mod.getName()) == null) {
                    DownloadThread dt = new DownloadThread(links, mod, modsFolder);
                    downloadWaitings.add(dt);
                } else {
                    log.info(String.format("%s already exists, set up link.", mod.getName()));
                    Mod mod2 = localManifest.searchFileName(mod.getName());
                    links.add(new CFMLink(mod2, mod));
                }
            } catch (Exception e) {
                log.error("Problem occurred in downloading mod. Skipping...", e);
            }
        }

        int downloadTotal = downloadWaitings.size();
        log.info("Downloading, please wait...");
        while ((downloadWaitings.size() > 0) || (downloadings.size() > 0)) {
            while (downloadings.size() < serverConfig.maxThreadCount && downloadWaitings.size() > 0) {
                DownloadThread dt = downloadWaitings.poll();
                if (dt != null) {
                    dt.start();
                    downloadings.add(dt);
                }
            }
            Iterator<DownloadThread> iterator = downloadings.iterator();
            while (iterator.hasNext()) {
                DownloadThread dt = iterator.next();
                if (dt.completed) {
                    dt.interrupt();
                    iterator.remove();
                }
            }
            int percent = 100 - ((downloadWaitings.size() + downloadings.size()) * 100) / downloadTotal;
            outputProgress(percent);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        System.out.println();

        log.info("Download complete. making link file...");
        File linkListFile = new File(CFModManager.DEFAULT_LINKLIST_CONFIG_FILE);
        if (!linkListFile.exists()) {
            try {
                if (!linkListFile.createNewFile()) {
                    log.error("Unable to create link list file");
                }
            } catch (IOException e) {
                log.error("Unable to create link list file", e);
            }
        }
        try {
            Utils.writeFile(linkListFile, "UTF-8", prettyGson.toJson(links));
        } catch (IOException e) {
            log.error("Unable to write link list file", e);
        }
    }

    class DownloadThread extends Thread {
        private List<CFMLink> links;
        private CurseForgeMod cfMod;
        private File modsFolder;

        private boolean completed = false;

        public DownloadThread(List<CFMLink> links, CurseForgeMod cfMod, File modsFolder) {
            this.links = links;
            this.cfMod = cfMod;
            this.modsFolder = modsFolder;
        }

        @Override
        public void run() {
            try {
                CFMLink link = cfMod.download(modsFolder);
                synchronized (links) {
                    links.add(link);
                }
            } catch (Exception e) {
                synchronized (log) {
                    System.out.print(repeatChar('\b', progressBarLen));
                    log.error("Problem occurred in downloading mod. Skipping...", e);
                }
                outputProgress(percent);
            }
            completed = true;
        }
    }
}