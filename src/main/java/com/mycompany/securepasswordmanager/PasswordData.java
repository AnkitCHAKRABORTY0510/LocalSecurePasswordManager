package com.mycompany.securepasswordmanager;

/**
 * Represents the data associated with a password entry.
 */
public class PasswordData {
    private String encryptedPassword;
    private String salt;
    private String url;
    private String account;
    private String description;  
    private int id;
    private String decryptedPassword;
    
    // Protected methods to set and get decryptedPassword
    protected void setDecryptedPassword(String decryptedPassword) {
        this.decryptedPassword = decryptedPassword;
    }
    
    protected String getDecryptedPassword() {
        return decryptedPassword;
    }

    // Getters and setters for other properties
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    // Constructors
    public PasswordData(int id, String url, String account, String encryptedPassword, String decryptedPassword, String salt, String description) {
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.url = url;
        this.id = id;
        this.decryptedPassword = decryptedPassword;
        this.account = account;
        this.description = description;
    }
    
    public PasswordData(String encryptedPassword, String salt) {
        log("Encrypted Password: " + encryptedPassword);
        log("Salt: " + salt);
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
    }

    // Getters for password-related fields
    public String getEncryptedPassword() {
        log("Encrypted Password: " + encryptedPassword); // Log for debugging
        return encryptedPassword;
    }

    public String getSalt() {
        log("Salt: " + salt); // Log for debugging
        return salt;
    }
    
    public String getUrl() {
        log("URL: " + url); // Log for debugging
        return url;
    }

    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    // Utility method for logging
    private void log(String message) {
        System.out.println(message); // Replace with a proper logger if needed
    }
}
