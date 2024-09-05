package io.github.citrussin.modupdater;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
    public static List<String> listZipFiles(File f) throws IOException {
        List<String> paths = new LinkedList<>();

        ZipInputStream zis = new ZipInputStream(Files.newInputStream(f.toPath()));
        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            paths.add(entry.getName());
        }

        zis.close();
        return paths;
    }

    public static ZipEntry zipLocateToFile(ZipInputStream zis, String path) throws IOException {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals(path)) {
                break;
            }
        }
        return entry;
    }

    public static String readFile(File file) throws IOException {
        return readFile(file, StandardCharsets.UTF_8);
    }

    public static String readFile(File file, Charset charset) throws IOException {
        int length = (int)file.length();
        if (length > 0) {
            byte[] buffer = new byte[length];
            FileInputStream inputStream = new FileInputStream(file);
            int realLen = inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, 0, realLen, charset);
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

    public static String calculateFileHash(File file, Supplier<MessageDigest> hashAlgorithm) throws IOException {
        MessageDigest digestion = hashAlgorithm.get();
        FileInputStream inputStream = new FileInputStream(file);
        DigestInputStream digestStream =
                new DigestInputStream(inputStream, digestion);
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

    public static Map<String, String> calculateFileHash(File file, List<HashAlgorithm> hashAlgorithms)
            throws IOException {
        Map<String, String> map = new TreeMap<>();
        for (HashAlgorithm hashAlgorithm : hashAlgorithms) {
            map.put(hashAlgorithm.getName(), calculateFileHash(file, hashAlgorithm));
        }
        return map;
    }

    public static <T> Set<T> getIntersection(Set<T> s1, Set<T> s2) {
        Set<T> setResult = new HashSet<>();
        s1.forEach(e -> {
            if (s2.contains(e)) {
                setResult.add(e);
            }
        });
        return setResult;
    }
}
