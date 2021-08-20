package io.github.micrafast.modupdater;

import com.google.gson.annotations.Expose;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class Mod {
    protected static final Log log = LogFactory.getLog(Mod.class);

    @Expose
    private String md5;

    @Expose
    private String fileName;

    public File localFile;

    public Mod(File file) {
        fileName = file.getName();
        localFile = file;
        calculateMD5();
    }

    protected void calculateMD5() {
        try {
            FileInputStream inputStream = new FileInputStream(localFile);
            DigestInputStream digestStream =
                    new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
            byte[] buffer = new byte[4096];
            while (true) {
                if (digestStream.read(buffer) <= -1) break;
            }
            MessageDigest md = digestStream.getMessageDigest();
            byte[] digest = md.digest();
            md5 = Hex.encodeHexString(digest);
            digestStream.close();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("MD5 Calculation failed: ", e);
        }
    }

    public String getMD5HexString() {
        return md5;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object mod2) {
        if (mod2 instanceof Mod) {
            return this.getMD5HexString().equalsIgnoreCase(((Mod) mod2).getMD5HexString());
        }
        return false;
    }

    @Override
    public String toString() {
        return getFileName();
    }

    public boolean equalsByFileName(Mod mod2) {
        return this.getFileName().equals(mod2.getFileName());
    }

    public static List<Mod> getModList(File directory) {
        List<Mod> mods = new LinkedList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            String suffix = fileName.substring(fileName.length()-4);
            if (suffix.equalsIgnoreCase(".jar")) {
                mods.add(new Mod(file));
            }
        }
        return mods;
    }

    public static Mod[] getModArray(File directory) {
        List<Mod> mods = getModList(directory);
        Mod[] list = new Mod[mods.size()];
        return mods.toArray(list);
    }
}
