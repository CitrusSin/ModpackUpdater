package io.github.citrussin.modupdater.server.redirection.setup.modrinth;

import com.google.gson.annotations.Expose;
import io.github.citrussin.modupdater.GsonManager;

import java.io.*;
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
        ModrinthPack pack = null;
        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry = zipStream.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            if (name.equals(INDEX_JSON_NAME) && !entry.isDirectory()) {
                InputStreamReader reader = new InputStreamReader(zipStream);
                pack = fromReaderJson(reader);
                zipStream.closeEntry();
                break;
            }
            zipStream.closeEntry();
            entry = zipStream.getNextEntry();
        }
        zipStream.close();

        return pack;
    }
}
