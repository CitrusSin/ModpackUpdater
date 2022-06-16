package io.github.citrussin.modupdater.server.redirection.setup.curseforge.api;

import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.network.NetworkUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CurseforgeAPI {
    private static final Log log = LogFactory.getLog(CurseforgeAPI.class);
    private static final String MOD_DOWNLOAD_URL_API = "https://addons-ecs.forgesvc.net/api/v2/addon/{addonID}/file/{fileID}/download-url";

    public static String getCFModName(CurseforgeMod cfMod) {
        String[] tokens = cfMod.getUrl().split("/");
        try {
            return URLDecoder.decode(tokens[tokens.length-1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return tokens[tokens.length-1];
        }
    }

    public static Mod downloadCFMod(CurseforgeMod cfMod, File targetDir) throws IOException {
        String fileName = cfMod.getName();
        File targetFile = new File(targetDir, fileName);
        NetworkUtils.download(cfMod.getUrl(), targetFile);
        return new Mod(targetFile);
    }

    public static String getCFModUrl(CurseforgeMod cfMod) {
        if (cfMod.url == null) {
            String api = MOD_DOWNLOAD_URL_API
                    .replace("{addonID}", Long.toString(cfMod.projectID))
                    .replace("{fileID}", Long.toString(cfMod.fileID));
            try {
                // This api is not so well that its url provided will not encode filename
                // So I have to do it myself
                String rawUrl = NetworkUtils.getString(api);
                String[] paths = rawUrl.split("/");
                String fileName = paths[paths.length - 1];
                String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
                cfMod.url = rawUrl.replace(fileName, encodedFileName);
                return cfMod.url;
            } catch (IOException e) {
                log.error(
                        String.format(
                                "Failed to get CurseForge mod name. projectID: %d, fileID: %d",
                                cfMod.projectID,
                                cfMod.fileID
                        ), e);
                return null;
            }
        } else {
            return cfMod.url;
        }
    }
}