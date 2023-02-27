package io.github.citrussin.modupdater;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    private static final MessageDigest hashAlgorithm = DigestUtils.getMd5Digest();

    public static String readFile(File file) throws IOException {
        return readFile(file, StandardCharsets.UTF_8);
    }

    public static String readFile(File file, Charset charset) throws IOException {
        int length = (int)file.length();
        if (length > 0) {
            byte[] buffer = new byte[length];
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, charset);
        } else {
            return "";
        }
    }

    public static void writeFile(File file, String ctx) throws IOException {
        writeFile(file, ctx, StandardCharsets.UTF_8);
    }

    public static void writeFile(File file, String ctx, Charset charset) throws IOException {
        if (!file.exists()) {
            boolean result = file.createNewFile();
            if (!result) {
                throw new IOException("File.createNewFile() failed");
            }
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false), charset);
        writer.write(ctx);
        writer.close();
    }

    public static String calculateFileHash(File file) throws NoSuchAlgorithmException, IOException {
        FileInputStream inputStream = new FileInputStream(file);
        DigestInputStream digestStream =
                new DigestInputStream(inputStream, hashAlgorithm);
        byte[] buffer = new byte[4096];
        while (true) {
            if (digestStream.read(buffer) <= -1) break;
        }
        MessageDigest md = digestStream.getMessageDigest();
        byte[] digest = md.digest();
        String hash = Hex.encodeHexString(digest);
        digestStream.close();
        return hash;
    }
}
