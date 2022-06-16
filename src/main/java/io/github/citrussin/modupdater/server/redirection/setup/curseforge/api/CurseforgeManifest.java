package io.github.citrussin.modupdater.server.redirection.setup.curseforge.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

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

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static CurseforgeManifest fromJson(String json) {
        return gson.fromJson(json, CurseforgeManifest.class);
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