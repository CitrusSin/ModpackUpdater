package io.github.micrafast.modupdater.cfapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.List;

public class CurseForgeManifest {
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
    public List<CurseForgeMod> files;

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static CurseForgeManifest fromJson(String json) {
        return gson.fromJson(json, CurseForgeManifest.class);
    }
}
