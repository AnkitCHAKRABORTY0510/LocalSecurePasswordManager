package com.mycompany.securepasswordmanager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {

    // Protected constructor to prevent instantiation
    protected SecurityUtils() {}

    /**
     * Generates a salt for hashing.
     * 
     * @return A Base64 encoded salt
     */
    protected static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a given input using SHA-256 with a provided salt.
     * 
     * @param input The data to be hashed
     * @param salt The salt to use in the hashing process
     * @return The hashed data in Base64 format
     * @throws NoSuchAlgorithmException If SHA-256 algorithm is not available
     */
    protected static String hashData(String input, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        byte[] hashedData = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedData);
    }

    /**
     * Verifies if the provided input, when hashed with the provided salt,
     * matches the expected hash.
     * 
     * @param input The data to be verified
     * @param salt The salt used during hashing
     * @param expectedHash The expected hash value to match against
     * @return True if the input matches the expected hash, false otherwise
     * @throws NoSuchAlgorithmException If SHA-256 algorithm is not available
     */
    protected static boolean verifyData(String input, String salt, String expectedHash) throws NoSuchAlgorithmException {
        String hash = hashData(input, salt);
        return hash.equals(expectedHash);
    }
}
