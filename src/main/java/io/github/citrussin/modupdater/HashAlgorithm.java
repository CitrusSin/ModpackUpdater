package io.github.citrussin.modupdater;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

public class HashAlgorithm implements Supplier<MessageDigest> {
    public static final HashAlgorithm SHA512 = new HashAlgorithm("SHA-512");
    public static final HashAlgorithm SHA256 = new HashAlgorithm("SHA-256");
    public static final HashAlgorithm MD5 = new HashAlgorithm("MD5");

    private final String algorithmName;

    public HashAlgorithm(String algorithmName) {
        try {
            MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.algorithmName = algorithmName;
    }

    public String getName() {
        return algorithmName;
    }

    @Override
    public String toString() {
        return algorithmName;
    }

    @Override
    public MessageDigest get() {
        try {
            return MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            // NOT EXPECTED
            throw new RuntimeException(e);
        }
    }
}
