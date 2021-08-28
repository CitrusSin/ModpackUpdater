package io.github.micrafast.modupdater.cfapi;

import com.google.gson.annotations.Expose;
import io.github.micrafast.modupdater.Mod;

public class CFMLink {
    @Expose
    protected Mod localMod;

    @Expose
    protected CurseForgeMod curseForgeContext;

    public Mod getLocalMod() {
        return localMod;
    }

    public CurseForgeMod getCurseForgeContext() {
        return curseForgeContext;
    }

    public CFMLink(Mod localMod, CurseForgeMod cfMod) {
        this.localMod = localMod;
        this.curseForgeContext = cfMod;
    }
}
