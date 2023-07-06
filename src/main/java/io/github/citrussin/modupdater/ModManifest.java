package io.github.citrussin.modupdater;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import io.github.citrussin.modupdater.network.NetworkUtils;
import io.github.citrussin.modupdater.server.redirection.ModProvider;
import io.github.citrussin.modupdater.server.redirection.ModRedirectionProvider;
import io.github.citrussin.modupdater.server.redirection.ModUploadProvider;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

public class ModManifest {
    @Expose
    @SerializedName("common")
    public List<Mod> commonMods;

    @Expose
    @SerializedName("optional")
    public List<Mod> optionalMods;

    public List<ModRedirectionProvider> modRedirectionProviders;

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
        return GsonManager.gson.toJson(this);
    }

    public boolean isRemote() {
        return (remoteUrl != null);
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public Mod searchByHash(String hashString) {
        Mod target = null;
        // Try every algorithm
        for (MessageDigest hashAlgorithm : Mod.HASH_ALGORITHMS) {
            target = searchByHash(hashAlgorithm, hashString);
            if (target != null) {
                break;
            }
        }
        return target;
    }

    public Mod searchByHash(MessageDigest hashAlgorithm, String hashString) {
        for (Mod mod : commonMods) {
            if (mod.getHashString(hashAlgorithm).equalsIgnoreCase(hashString)) {
                return mod;
            }
        }
        for (Mod mod : optionalMods) {
            if (mod.getHashString(hashAlgorithm).equalsIgnoreCase(hashString)) {
                return mod;
            }
        }
        return null;
    }

    public Mod searchFilename(String filename) {
        for (Mod mod : commonMods) {
            if (mod.getFilename().equals(filename)) {
                return mod;
            }
        }
        for (Mod mod : optionalMods) {
            if (mod.getFilename().equals(filename)) {
                return mod;
            }
        }
        return null;
    }

    public ModProvider getProviderByHash(MessageDigest hashAlgorithm, String hash) {
        if (isRemote()) {
            return null;
        }
        for (ModRedirectionProvider redirection : modRedirectionProviders) {
            if (
                    redirection.hashValues.containsKey(hashAlgorithm.getAlgorithm()) &&
                    redirection.hashValues.get(hashAlgorithm.getAlgorithm()).equalsIgnoreCase(hash)
            ) {
                return redirection;
            }
        }
        return getProvider(this.searchByHash(hashAlgorithm, hash));
    }

    public ModProvider getProviderByFilename(String filename) {
        return this.getProvider(this.searchFilename(filename));
    }

    public ModProvider getProvider(Mod mod) {
        if (isRemote() || mod == null) {
            return null;
        }
        for (ModRedirectionProvider redirection : modRedirectionProviders) {
            if (mod.checkHashValues(redirection.hashValues)) {
                return redirection;
            }
        }
        return new ModUploadProvider(mod);
    }

    public void loadRedirectionList(String redirectionJson) {
        this.modRedirectionProviders = GsonManager.gson.fromJson(redirectionJson, new TypeToken<List<ModRedirectionProvider>>(){}.getType());
    }

    public static ModManifest fromRemote(String url) throws IOException {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String json = NetworkUtils.getString(url + "/mods/list");
        ModManifest manifest = GsonManager.gson.fromJson(json, ModManifest.class);
        manifest.remoteUrl = url;
        return manifest;
    }
}
