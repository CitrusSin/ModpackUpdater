package io.github.micrafast.modupdater;

import java.io.*;
import java.util.List;

public class Utils {
    public static String readFile(File file, String charsetName) throws IOException {
        int length = (int)file.length();
        if (length > 0) {
            byte[] buffer = new byte[length];
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, charsetName);
        } else {
            return "";
        }
    }

    public static void writeFile(File file, String charsetName, String ctx) throws IOException {
        if (!file.exists()) {
            boolean result = file.createNewFile();
            if (!result) {
                throw new IOException("File.createNewFile() failed");
            }
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false), charsetName);
        writer.write(ctx);
        writer.close();
    }

    public static boolean containsMod(List<Mod> modList, Mod mod) {
        for (Mod m : modList) {
            String  hash1 = m.getMD5HexString(),
                    hash2 = mod.getMD5HexString();
            if (hash1.equals(hash2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsModByFileName(List<Mod> modList, Mod mod) {
        for (Mod m : modList) {
            String  fileName1 = m.getFileName(),
                    fileName2 = mod.getFileName();
            if (fileName1.equals(fileName2)) {
                return true;
            }
        }
        return false;
    }

}
