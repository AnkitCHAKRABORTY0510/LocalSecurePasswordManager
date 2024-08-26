package com.mycompany.securepasswordmanager;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordGenerator {

    public static PasswordData generatePassword(int passwordLength) {
        try {
            SecretKey secretKey = generateSecretKey();

            System.out.println("Secret Key: " + encodeKey(secretKey));
            // Generate a random password of specified length
            String password = generateRandomPassword(passwordLength);
            System.out.println("Generated Password: " + password);

            // Encrypt password with AES
            String encryptedPassword = encryptPassword(password, secretKey);

            // Return encrypted password and the secret key encoded as base64
            return new PasswordData(encryptedPassword, encodeKey(secretKey));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom()); // Use SecureRandom for more robust key generation
        return keyGenerator.generateKey();
    }

    private static String generateRandomPassword(int length) {
        // Using SecureRandom for more secure and cross-platform randomness
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=";

        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    private static String encryptPassword(String password, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String encodeKey(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
