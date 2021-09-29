package io.github.micrafast.modupdater.server.curseforge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.micrafast.modupdater.Mod;
import io.github.micrafast.modupdater.Utils;
import io.github.micrafast.modupdater.cfapi.CFMLink;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CFModManager {
    protected static final String DEFAULT_LINKLIST_CONFIG_FILE = "modupdater_server_curseforge_modlinks.json";
    private static final Gson gson = new Gson();

    private List<CFMLink> links;

    public static boolean hasCFModLinks() {
        return new File(DEFAULT_LINKLIST_CONFIG_FILE).exists();
    }

    public CFModManager() throws IOException {
        this(new File(DEFAULT_LINKLIST_CONFIG_FILE));
    }

    public CFModManager(File configFile) throws IOException {
        String json = Utils.readFile(configFile, "UTF-8");
        Type listType = new TypeToken<List<CFMLink>>(){}.getType();
        links = gson.fromJson(json, listType);
    }

    public List<CFMLink> getLinks(){
        return links;
    }

    public CFMLink getLink(Mod mod) {
        for (CFMLink link : links) {
            if (link.getLocalMod().equals(mod)) {
                return link;
            }
        }
        return null;
    }

    public boolean hasLink(Mod mod) {
        return getLink(mod) != null;
    }
}
