package io.github.citrussin.modupdater.server.redirection.setup.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.github.citrussin.modupdater.GsonManager;
import io.github.citrussin.modupdater.Mod;
import io.github.citrussin.modupdater.ModUpdaterMain;
import io.github.citrussin.modupdater.Utils;
import io.github.citrussin.modupdater.network.NetworkUtils;
import io.github.citrussin.modupdater.server.redirection.setup.RedirectionInitalizer;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ModrinthRedirectionInitalizer extends RedirectionInitalizer {
    private static final String MODRINTH_API = "https://api.modrinth.com";

    public ModrinthRedirectionInitalizer() throws IOException {
        super();
    }

    private String modrinthUrlGet(String url) throws IOException {
        return NetworkUtils.getStringWithUserAgent(url, "CitrusSin/ModpackUpdater/" + ModUpdaterMain.SERVICE_VER);
    }

    @Override
    protected String urlFromLocalMod(Mod mod) throws IOException {
        String hashString = mod.getHashString(DigestUtils.getSha512Digest());

        String requestUrl =
                MODRINTH_API + String.format(
                        "/v2/version_file/%s?algorithm=sha512",
                        hashString
                );

        String json = modrinthUrlGet(requestUrl);

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        assert obj.has("files");
        JsonArray files = obj.getAsJsonArray("files");

        String url = null;
        for (JsonElement file : files) {
            if (!file.isJsonObject()) {
                continue;
            }
            JsonObject fileObj = file.getAsJsonObject();
            assert fileObj.has("hashes");
            JsonObject hashes = fileObj.getAsJsonObject("hashes");

            String sha512 = hashes.getAsJsonPrimitive("sha512").getAsString();

            if (sha512.equalsIgnoreCase(hashString)) {
                url = fileObj.getAsJsonPrimitive("url").getAsString();
            }
        }

        return url;
    }
}
