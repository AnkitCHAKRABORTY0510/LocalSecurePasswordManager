/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


//functions to generate random password not the encryption part
public class PasswordGenerator {

    public static PasswordData generatePassword(int passwordLength) {
        try {
            SecretKey secretKey = generateSecretKey();
            
            System.out.println("Secret Key " +secretKey);
            // Generate a random password of specified length
            String password = generateRandomPassword(passwordLength);
            System.out.println("Generated Password "+ password);

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
        keyGenerator.init(256); // AES-256
        return keyGenerator.generateKey();
    }

    private static String generateRandomPassword(int length) {
        // Implement your random password generation logic here
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append((char) ((Math.random() * (126 - 33)) + 33)); // Printable ASCII characters
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

