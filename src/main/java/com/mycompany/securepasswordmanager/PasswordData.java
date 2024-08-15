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
    private String account;
    private String description;  
    private int id;
    private String decryptedPassword;
    
    protected  void setdecryptedPassword(String decryptedPassword)
    {
        this.decryptedPassword=decryptedPassword;
    }
    
    protected String getdecryptedPassword()
    {
        return decryptedPassword;
    }

    // Existing getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public PasswordData(int id, String url,String account, String encryptedPassword,String decryptPassword, String salt, String description) {
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.url= url;
        this.id=id;
        this.decryptedPassword=decryptPassword;
        this.account=account;
        this.description=description;
    }
    
    public PasswordData(String encryptedPassword, String salt) {
        System.out.println("encryptedPassword "+encryptedPassword);
        System.out.println("salt " + salt);
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
    }

    public String getEncryptedPassword() {
        System.out.println("encryptedPassword "+encryptedPassword);// my generated password
        return encryptedPassword;
    }

    public String getSalt() {
        System.out.println("salt "+salt);//check///this data is taken out so that i can decrypt my generated password
        return salt;
    }
    
    public String getUrl() {
        System.out.println(url);
        return url;
        
    }
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String Account) {
        this.account = Account;
    }
}