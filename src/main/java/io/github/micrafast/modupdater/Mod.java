package io.github.micrafast.modupdater;

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

    private String md5;
    private String fileName;

    public Mod(File file) {
        fileName = file.getName();
        calculateMD5(file);
    }

    protected void calculateMD5(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            DigestInputStream digestStream =
                    new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
            MessageDigest md = digestStream.getMessageDigest();
            byte[] digest = md.digest();
            md5 = Hex.encodeHexString(digest);
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

    public static Mod[] getModList(File directory) {
        List<Mod> mods = new LinkedList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            String suffix = fileName.substring(fileName.length()-4);
            if (suffix.equalsIgnoreCase(".jar")) {
                mods.add(new Mod(file));
            }
        }
        Mod[] list = new Mod[mods.size()];
        return mods.toArray(list);
    }
}
