package com.demoproject.utils;

import com.demoproject.exceptions.FrameworkException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256/GCM encryption for secrets stored as {@code ENC(...)} in properties files.
 * <p>
 * The key is a Base64-encoded AES key read from the {@value #SECRET_KEY_ENV} environment
 * variable; it is never stored in the repository. Cipher text format:
 * {@code Base64(12-byte IV || cipher text || 16-byte GCM tag)}.
 * <p>
 * Command line usage (after {@code mvn compile}):
 * <pre>
 * java -cp target/classes com.demoproject.utils.EncryptionUtils generate-key
 * java -cp target/classes com.demoproject.utils.EncryptionUtils encrypt "my secret"
 * </pre>
 */
public final class EncryptionUtils {

    /** Environment variable holding the Base64 AES key. */
    public static final String SECRET_KEY_ENV = "DEMO_SECRET_KEY";

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_SIZE_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private EncryptionUtils() {
    }

    /**
     * Encrypts with the key from {@value #SECRET_KEY_ENV}.
     *
     * @param plainText secret to encrypt
     * @return Base64 cipher text (wrap it as {@code ENC(...)} in properties files)
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, loadKey());
    }

    /**
     * @param plainText secret to encrypt
     * @param key       AES key
     * @return Base64 cipher text
     */
    public static String encrypt(String plainText, SecretKey key) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv).put(encrypted).array());
        } catch (GeneralSecurityException e) {
            throw new FrameworkException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts with the key from {@value #SECRET_KEY_ENV}.
     *
     * @param cipherText Base64 cipher text (without the {@code ENC(...)} wrapper)
     * @return plain text
     */
    public static String decrypt(String cipherText) {
        return decrypt(cipherText, loadKey());
    }

    /**
     * @param cipherText Base64 cipher text
     * @param key        AES key
     * @return plain text
     */
    public static String decrypt(String cipherText, SecretKey key) {
        try {
            byte[] payload = Base64.getDecoder().decode(cipherText.trim());
            if (payload.length <= IV_LENGTH_BYTES) {
                throw new FrameworkException("cipher text is too short to be valid");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, payload, 0, IV_LENGTH_BYTES));
            byte[] plain = cipher.doFinal(payload, IV_LENGTH_BYTES, payload.length - IV_LENGTH_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new FrameworkException("cipher text is not valid Base64", e);
        } catch (GeneralSecurityException e) {
            throw new FrameworkException("decryption failed - was it encrypted with the key in "
                    + SECRET_KEY_ENV + "?", e);
        }
    }

    /**
     * @return a new random Base64-encoded AES-256 key
     */
    public static String generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            generator.init(KEY_SIZE_BITS, RANDOM);
            return Base64.getEncoder().encodeToString(generator.generateKey().getEncoded());
        } catch (GeneralSecurityException e) {
            throw new FrameworkException("Unable to generate AES key", e);
        }
    }

    private static SecretKey loadKey() {
        String encoded = System.getenv(SECRET_KEY_ENV);
        if (encoded == null || encoded.isBlank()) {
            throw new FrameworkException("Environment variable " + SECRET_KEY_ENV + " is not set; it is required "
                    + "to decrypt ENC(...) values. Generate one with: "
                    + "java -cp target/classes com.demoproject.utils.EncryptionUtils generate-key");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encoded.trim());
        } catch (IllegalArgumentException e) {
            throw new FrameworkException(SECRET_KEY_ENV + " is not valid Base64", e);
        }
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new FrameworkException(SECRET_KEY_ENV + " must decode to 16, 24 or 32 bytes but was "
                    + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * CLI entry point: {@code generate-key} or {@code encrypt <plainText>}.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 1 && "generate-key".equals(args[0])) {
            System.out.println(generateKey());
        } else if (args.length == 2 && "encrypt".equals(args[0])) {
            System.out.println("ENC(" + encrypt(args[1]) + ")");
        } else {
            System.out.println("Usage:\n  EncryptionUtils generate-key\n  EncryptionUtils encrypt <plainText>"
                    + "\n(encrypt reads the key from the " + SECRET_KEY_ENV + " environment variable)");
        }
    }
}
