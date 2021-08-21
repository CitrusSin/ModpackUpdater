package io.github.micrafast.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.micrafast.modupdater.network.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ModManifest {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Expose
    @SerializedName("common")
    public List<Mod> commonMods;

    @Expose
    @SerializedName("optional")
    public List<Mod> optionalMods;

    private String remoteUrl;

    public ModManifest(List<Mod> commonMods, List<Mod> optionalMods) {
        this.commonMods = commonMods;
        this.optionalMods = optionalMods;
    }

    public ModManifest(File commonDir, File optionalDir) {
        this(Mod.getModList(commonDir), Mod.getModList(optionalDir));
    }

    public ModManifest(String commonDir, String optionalDir) {
        this(new File(commonDir), new File(optionalDir));
    }

    public String getJson() {
        return gson.toJson(this);
    }

    public boolean isRemote() {
        return (remoteUrl != null);
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public Mod searchMD5(String md5) {
        for (Mod mod : commonMods) {
            if (mod.getMD5HexString().equalsIgnoreCase(md5)) {
                return mod;
            }
        }
        for (Mod mod : optionalMods) {
            if (mod.getMD5HexString().equalsIgnoreCase(md5)) {
                return mod;
            }
        }
        return null;
    }

    public static ModManifest fromRemote(String url) throws IOException {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String json = NetworkUtils.getString(url + "/mods/list");
        ModManifest manifest = gson.fromJson(json, ModManifest.class);
        manifest.remoteUrl = url;
        return manifest;
    }
}
