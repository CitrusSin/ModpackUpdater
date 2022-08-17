package io.github.citrussin.modupdater.server.redirection.setup.curseforge.api;

import com.google.gson.annotations.Expose;
import io.github.citrussin.modupdater.GsonManager;

import java.util.List;

public class CurseforgeManifest {
    @Expose
    public MinecraftVersion minecraft;

    @Expose
    public String manifestType;

    @Expose
    public String overrides;

    @Expose
    public int manifestVersion;

    @Expose
    public String version;

    @Expose
    public String author;

    @Expose
    public String name;

    @Expose
    public List<CurseforgeMod> files;

    public static CurseforgeManifest fromJson(String json) {
        return GsonManager.gsonExcludeWithoutExpose.fromJson(json, CurseforgeManifest.class);
    }

    static class MinecraftVersion {
        static class ModLoader {
            @Expose
            public String id;

            @Expose
            public boolean primary;
        }

        @Expose
        public String version;

        @Expose
        public List<ModLoader> modLoaders;
    }

}