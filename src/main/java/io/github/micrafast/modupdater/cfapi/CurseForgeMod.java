package io.github.micrafast.modupdater.cfapi;

import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.IOException;

public class CurseForgeMod {
    @Expose
    public long projectID;

    @Expose
    public long fileID;

    @Expose
    public boolean required;

    @Expose
    String url;

    public CFMLink download(File dir) throws IOException {
        return new CFMLink(CurseForgeAPI.downloadCFMod(this, dir), this);
    }

    public String getName() {
        return CurseForgeAPI.getCFModName(this);
    }

    public String getUrl() {
        return CurseForgeAPI.getCFModUrl(this);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
