package io.github.citrussin.modupdater.server.redirection.setup.curseforge;

import com.google.gson.annotations.Expose;

public class CurseforgeMod {
    @Expose
    public long projectID;

    @Expose
    public long fileID;

    @Expose
    public boolean required;

    @Expose
    String url;


    public String getName() {
        return CurseforgeAPI.getCFModName(this);
    }

    public String getUrl() {
        return CurseforgeAPI.getCFModUrl(this);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}