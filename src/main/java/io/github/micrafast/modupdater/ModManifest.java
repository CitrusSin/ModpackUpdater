package io.github.micrafast.modupdater;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.List;

public class ModManifest {
    @SerializedName("common")
    public List<Mod> commonMods;

    @SerializedName("optional")
    public List<Mod> optionalMods;

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
}
