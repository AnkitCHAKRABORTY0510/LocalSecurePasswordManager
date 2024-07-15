/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

/**
 *
 * @author mac
 */
public class PasswordData {
    private String encryptedPassword;
    private String salt;

    public PasswordData(String encryptedPassword, String salt) {
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
    }

    public String getEncryptedPassword() {
        System.out.println(encryptedPassword);//check
        return encryptedPassword;
    }

    public String getSalt() {
        System.out.println(salt);//check
        return salt;
    }
}