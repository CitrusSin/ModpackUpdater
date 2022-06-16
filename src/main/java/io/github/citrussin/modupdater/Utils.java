package io.github.citrussin.modupdater;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String calculateMD5(File file) throws NoSuchAlgorithmException, IOException {
        FileInputStream inputStream = new FileInputStream(file);
        DigestInputStream digestStream =
                new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
        byte[] buffer = new byte[4096];
        while (true) {
            if (digestStream.read(buffer) <= -1) break;
        }
        MessageDigest md = digestStream.getMessageDigest();
        byte[] digest = md.digest();
        String md5 = Hex.encodeHexString(digest);
        digestStream.close();
        return md5;
    }
}
