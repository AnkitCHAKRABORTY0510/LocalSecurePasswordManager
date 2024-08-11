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
    private String url;
    private String description;  
    private int id;  

    // Existing getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public PasswordData(int id, String url, String encryptedPassword, String salt, String description) {
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.url= url;
        this.id=id;
        this.description=description;
    }
    
    public PasswordData(String encryptedPassword, String salt) {
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
    }

    public String getEncryptedPassword() {
        System.out.println(encryptedPassword);//check///this data is taken out so that i can decrypt my generated password
        return encryptedPassword;
    }

    public String getSalt() {
        System.out.println(salt);//check///this data is taken out so that i can decrypt my generated password
        return salt;
    }
    
    public String getUrl() {
        System.out.println(url);
        return url;
        
    }
}