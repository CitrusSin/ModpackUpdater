package io.github.citrussin.modupdater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipInputStream;

public class Mod{
    protected static final Log log = LogFactory.getLog(Mod.class);

    public static final MessageDigest[] HASH_ALGORITHMS = {
        DigestUtils.getMd5Digest(),
        DigestUtils.getSha256Digest(),
        DigestUtils.getSha512Digest()
    };

    public static final MessageDigest DEFAULT_HASH = DigestUtils.getSha512Digest();

    private static final String FORGE_MOD_DESCRIPTION_FILE_PATH = "META-INF/mods.toml";
    private static final String FABRIC_MOD_DESCRIPTION_FILE_PATH = "fabric.mod.json";

    @Expose
    private final TreeMap<String, String> hashValues;

    @Expose
    private final String fileName;

    public final File localFile;

    private LoaderType modLoaderType = LoaderType.UNKNOWN;
    private String modid = null;
    private String version = null;

    public Mod(File file) {
        this(file, HASH_ALGORITHMS);
    }

    public Mod(File file, MessageDigest[] algorithms) {
        fileName = file.getName();
        localFile = file;
        hashValues = new TreeMap<>();
        for (MessageDigest algorithm : algorithms) {
            calculateHashString(algorithm);
        }
        if (!hashValues.containsKey(DEFAULT_HASH.getAlgorithm())) {
            calculateHashString(DEFAULT_HASH);
        }
    }

    private void calculateHashString(MessageDigest hashAlgorithm) {
        try {
            String algorithmName = hashAlgorithm.getAlgorithm();
            hashValues.put(algorithmName, Utils.calculateFileHash(localFile, hashAlgorithm));
        } catch (IOException e) {
            log.error("Hash calculation failed: ", e);
        }
    }

    public String getHashString(MessageDigest hashAlgorithm) {
        if (!hashValues.containsKey(hashAlgorithm.getAlgorithm())) {
            calculateHashString(hashAlgorithm);
        }
        return hashValues.get(hashAlgorithm.getAlgorithm());
    }

    public String getFilename() {
        return fileName;
    }

    public boolean checkHashValues(Map<String, String> hashValues) {
        Set<String> commonAlgorithmSet = Utils.getIntersection(hashValues.keySet(), this.hashValues.keySet());
        assert commonAlgorithmSet.contains(DEFAULT_HASH.getAlgorithm());

        boolean match = true;
        for (String algorithmName : commonAlgorithmSet) {
            if (!hashValues.get(algorithmName).equalsIgnoreCase(this.hashValues.get(algorithmName))) {
                match = false;
                break;
            }
        }
        return match;
    }

    public boolean localAvailable() {
        return this.localFile != null;
    }

    private void readForgeMod(InputStreamReader isr) throws IOException {
        BufferedReader br = new BufferedReader(isr);

        String line = br.readLine();
        while (!line.trim().startsWith("[[mods]]")) {
            line = br.readLine();
        }

        line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("[[")) {
                break;
            }
            String[] arr = line.split("=");
            if (arr.length == 2) {
                if ("modId".equals(arr[1])) {
                    this.modid = arr[1].replace("\"", "");
                } else if ("version".equals(arr[1])) {
                    this.version = arr[1].replace("\"", "");
                }
            }
            line = br.readLine();
        }
    }

    private void readFabricMod(InputStreamReader isr){
        JsonObject obj = JsonParser.parseReader(isr).getAsJsonObject();
        assert obj.has("schemaVersion");

        this.modid = obj.get("id").getAsString();
        this.version = obj.get("version").getAsString();
    }

    private void analyzeModLoaderType() throws IOException {
        List<String> internalFiles = Utils.listZipFiles(localFile);
        if (internalFiles.contains(FORGE_MOD_DESCRIPTION_FILE_PATH)) {
            this.modLoaderType = LoaderType.FORGE;
        } else if (internalFiles.contains(FABRIC_MOD_DESCRIPTION_FILE_PATH)) {
            this.modLoaderType = LoaderType.FABRIC;
        }
    }

    public LoaderType getModLoaderType() {
        if (!localAvailable()) {
            return LoaderType.UNKNOWN;
        }
        try {
            analyzeModLoaderType();
        } catch (IOException ignored) {
            // ignore
        }
        return modLoaderType;
    }

    private void processModFileInfo() throws IOException {
        if (!localAvailable()) {
            throw new FileNotFoundException("Mod file not available");
        }
        LoaderType loaderType = this.getModLoaderType();
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(this.localFile.toPath()));
        switch (loaderType) {
            case FORGE:
                Utils.zipLocateToFile(zis, FORGE_MOD_DESCRIPTION_FILE_PATH);
                readForgeMod(new InputStreamReader(zis));
                break;
            case FABRIC:
                Utils.zipLocateToFile(zis, FABRIC_MOD_DESCRIPTION_FILE_PATH);
                readFabricMod(new InputStreamReader(zis));
                break;
        }
        zis.close();
    }

    public String getModId() {
        try {
            if (this.modid == null) {
                processModFileInfo();
            }
        } catch (Exception ignored) {}
        if (this.modid != null) {
            return this.modid;
        } else {
            return this.getFilename();
        }
    }

    public String getVersion(){
        try {
            if (this.version == null) {
                processModFileInfo();
            }
        } catch (Exception ignored) {}
        if (this.version != null) {
            return this.version;
        } else {
            return "NULL";
        }
    }

    public Map<String, String> getHashValues() {
        return new TreeMap<>(this.hashValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mod) {
            Mod mod2 = (Mod)obj;
            return checkHashValues(mod2.hashValues);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getHashString(DEFAULT_HASH).toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return fileName;
    }

    public static List<Mod> getModList(File directory) {
        List<Mod> mods = new LinkedList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            log.error("Target is not a directory!");
            return mods;
        }
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".jar")) {
                mods.add(new Mod(file));
            }
        }
        return mods;
    }

    public enum LoaderType {
        UNKNOWN("Unknown"), FORGE("Forge"), FABRIC("Fabric");

        public final String name;

        LoaderType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
