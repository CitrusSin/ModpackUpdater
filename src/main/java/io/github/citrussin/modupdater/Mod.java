package io.github.citrussin.modupdater;

import com.google.gson.annotations.Expose;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class Mod{
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
            md5 = Utils.calculateMD5(localFile);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("MD5 Calculation failed: ", e);
        }
    }

    public String getMD5HexString() {
        return md5;
    }

    public String getFilename() {
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
        return getFilename();
    }

    public static List<Mod> getModList(File directory) {
        List<Mod> mods = new LinkedList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".jar")) {
                mods.add(new Mod(file));
            }
        }
        return mods;
    }
}
