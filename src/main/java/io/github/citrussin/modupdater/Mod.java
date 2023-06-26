package io.github.citrussin.modupdater;

import com.google.gson.annotations.Expose;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

public class Mod{
    protected static final Log log = LogFactory.getLog(Mod.class);

    public static final MessageDigest[] HASH_ALGORITHMS = {
        DigestUtils.getMd5Digest(),
        DigestUtils.getSha256Digest(),
        DigestUtils.getSha512Digest()
    };

    public static final MessageDigest DEFAULT_HASH = DigestUtils.getSha512Digest();

    @Expose
    private final Map<String, String> hashValues;

    @Expose
    private final String fileName;

    public final File localFile;

    public Mod(File file) {
        this(file, HASH_ALGORITHMS);
    }

    public Mod(File file, MessageDigest[] algorithms) {
        fileName = file.getName();
        localFile = file;
        hashValues = new HashMap<>();
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
