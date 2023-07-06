package io.github.citrussin.modupdater;

import com.google.gson.annotations.Expose;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Mod{
    protected static final Log log = LogFactory.getLog(Mod.class);

    public static final MessageDigest[] HASH_ALGORITHMS = {
        DigestUtils.getMd5Digest(),
        DigestUtils.getSha256Digest(),
        DigestUtils.getSha512Digest()
    };

    public static final MessageDigest DEFAULT_HASH = DigestUtils.getSha512Digest();

    public static final String MOD_DESCRIPTION_FILE_PATH = "META-INF/mods.toml";

    @Expose
    private final TreeMap<String, String> hashValues;

    @Expose
    private final String fileName;

    public final File localFile;

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
    }

    protected void calculateHashString(MessageDigest hashAlgorithm) {
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

    public boolean checkHashValue(MessageDigest hashAlgorithm, String value) {
        return value.equalsIgnoreCase(this.getHashString(hashAlgorithm));
    }

    public boolean checkHashValues(Map<String, String> hashValues) {
        Set<String> commonAlgorithmSet = Utils.getIntersection(hashValues.keySet(), this.hashValues.keySet());
        if (commonAlgorithmSet.isEmpty()) {
            return false;
        }
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

    /*
    protected void processModFileInfo() throws IOException {
        if (!localAvailable()) {
            throw new FileNotFoundException("Mod file not available");
        }
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(this.localFile.toPath()));

        ZipEntry entry = Utils.zipsMoveToEntryOfInternalPath(zis, MOD_DESCRIPTION_FILE_PATH);
        if (entry == null) {
            zis.close();
            throw new IOException("Mod file not regular");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(zis));

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

        zis.close();
        br.close();
    }

    public String getModId() throws IOException {
        if (this.modid == null) {
            processModFileInfo();
        }
        return this.modid;
    }

    public String getVersion() throws IOException {
        if (this.version == null) {
            processModFileInfo();
        }
        return this.version;
    }
    */

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
}
