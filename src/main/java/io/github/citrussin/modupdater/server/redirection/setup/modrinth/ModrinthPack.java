package io.github.citrussin.modupdater.server.redirection.setup.modrinth;

import com.google.gson.annotations.Expose;
import io.github.citrussin.modupdater.GsonManager;
import io.github.citrussin.modupdater.Utils;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModrinthPack {
    @Expose
    public int formatVersion;

    @Expose
    public String game;

    @Expose
    public String versionId;

    @Expose
    public String name;

    @Expose
    public List<ModrinthFile> files;

    @Expose
    public Map<String, String> dependencies;

    private static final String INDEX_JSON_NAME = "modrinth.index.json";

    public static ModrinthPack fromJson(String json) {
        return GsonManager.mapGson.fromJson(json, ModrinthPack.class);
    }

    public static ModrinthPack fromReaderJson(Reader reader) {
        return GsonManager.mapGson.fromJson(reader, ModrinthPack.class);
    }

    public static ModrinthPack fromMrPack(File file) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(file.toPath()));
        ZipEntry entry = Utils.zipLocateToFile(zipStream, INDEX_JSON_NAME);

        if (entry == null || entry.isDirectory()) {
            return null;
        }

        InputStreamReader reader = new InputStreamReader(zipStream);
        ModrinthPack pack = fromReaderJson(reader);
        zipStream.close();
        return pack;
    }
}
