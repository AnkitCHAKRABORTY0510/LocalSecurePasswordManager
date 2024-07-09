/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author mac
 */
package com.mycompany.securepasswordmanager;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class PasswordUtils {

    // Constants for PBKDF2
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 64 * 8;
    private static final int ITERATIONS = 10000;

    // Generate a hash for the password
    public static String hashPassword(String password) {
        try {
            byte[] salt = SecureRandomUtils.generateSalt(SALT_LENGTH);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            return enc.encodeToString(salt) + ":" + enc.encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing the password.", e);
        }
    }

    // Verify a password against the stored hash
    public static boolean checkPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            if (testHash.length != hash.length) {
                return false;
            }

            for (int i = 0; i < testHash.length; i++) {
                if (testHash[i] != hash[i]) {
                    return false;
                }
            }
            return true;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while verifying the password.", e);
        }
    }
}
