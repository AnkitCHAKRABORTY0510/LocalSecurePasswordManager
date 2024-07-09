package com.mycompany.securepasswordmanager;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author mac
 */


import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureRandomUtils {

    // Generate a secure random salt
    public static byte[] generateSalt(int length) {
        try {
            SecureRandom sr = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[length];
            sr.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while generating the salt.", e);
        }
    }
}
