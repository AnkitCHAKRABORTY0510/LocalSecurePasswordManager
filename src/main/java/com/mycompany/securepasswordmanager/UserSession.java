package com.mycompany.securepasswordmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String userID;
    private String firstName;
    private String lastName;
    private String emailId;
    private String phoneNumber;
    private String UserCreationDateTime;
    private String userCreationDate;
    private String userCreationTime;
    private String userPassword;
       
    
    
        
    // Fields to store encryption key and IV for encripting userid for key
    private SecretKey secretKey;
    private IvParameterSpec iv;
    // Fields to store encryption key and IV for decrypting data user
    private SecretKey usersecretKey;
    private IvParameterSpec useriv;

    private static String DB_URL_PREFIX = "jdbc:sqlite:data/users/";

    private UserSession() {
        // private constructor to prevent instantiation
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Method to reset the UserSession instance
    public static synchronized void logout() {
        instance = null;
    }
    
    //user secrets &IV to encript ther info into database
    public SecretKey getuserSecretKey() {
        return usersecretKey;
    }

    public void setuserSecretKey(SecretKey secretKey) {
        System.out.println(secretKey);
        this.usersecretKey = secretKey;
    }

    public IvParameterSpec getuserIv() {
        return useriv;
    }

    public void setuserIv(IvParameterSpec iv) {
        System.out.println(iv);
        this.useriv = iv;
    }


    // for key findinding
    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        
        this.secretKey = secretKey;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public void setIv(IvParameterSpec iv) {
        
        this.iv = iv;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserpassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserpassword() {
        return userPassword;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserCreationDate() {
        return userCreationDate;
    }

    public void setUserCreationDate(String userCreationDate) {
        this.userCreationDate = userCreationDate;
    }

    public String getUserCreationTime() {
        return userCreationTime;
    }
    
    

    public void setUserCreationTime(String userCreationTime) {
        this.userCreationTime = userCreationTime;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Helper method to split date and time
    public void setUserCreationDateTime(String dateTime) {
        this.UserCreationDateTime = dateTime;
        this.userCreationDate = dateTime.split("T")[0];
        this.userCreationTime = dateTime.split("T")[1];
    }

    public String getUserCreationDateTime() {
        return UserCreationDateTime;
    }

    public void initializeDatabase(String userID) {
        setUserID(userID);
        File directory = new File("data/users");
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory: " + "data/users");
                return;
            }
        }

        String dbUrl = DB_URL_PREFIX + userID + ".db";
        try (Connection connection = DriverManager.getConnection(dbUrl);
             Statement statement = connection.createStatement()) {

            String createUserTableSQL = "CREATE TABLE IF NOT EXISTS userinformation (" +
                                        "username TEXT PRIMARY KEY," +
                                        "first_name TEXT," +
                                        "last_name TEXT," +
                                        "user_creation_time TEXT," +
                                        "email_id TEXT," +
                                        "phone_number TEXT," +
                                        "user_id TEXT UNIQUE" +
                                        ")";

            String createPasswordTableSQL = "CREATE TABLE IF NOT EXISTS passwords (" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "url TEXT NOT NULL," +
                                            "Account TEXT," +
                                            "salt TEXT NOT NULL," +
                                            "encrypted_password TEXT NOT NULL," +
                                            "description TEXT," +
                                            "username TEXT NOT NULL," +
                                            "FOREIGN KEY(username) REFERENCES userinformation(username)" +
                                            ")";

            String createEncryptionKeysTableSQL = "CREATE TABLE IF NOT EXISTS encryption_keys (" +
                                                  "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                  "encrypted_secret_key TEXT NOT NULL," +
                                                  "encrypted_iv TEXT NOT NULL" +
                                                  ")";

            statement.executeUpdate(createUserTableSQL);
            statement.executeUpdate(createPasswordTableSQL);
            statement.executeUpdate(createEncryptionKeysTableSQL);
            
            // Retrieve the encryption key and IV
            retrieveEncryptionKeyAndIv(connection);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getDatabasePath(String userID) {
        return DB_URL_PREFIX + userID + ".db";
    }
    
    // Method to retrieve the encrypted secret key and IV from the database
    protected void retrieveEncryptionKeyAndIv(Connection connection) {
        String sql = "SELECT encrypted_secret_key, encrypted_iv FROM encryption_keys LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String encryptedSecretKey = rs.getString("encrypted_secret_key");
                String encryptedIv = rs.getString("encrypted_iv");

                // Decode the Base64 encoded values
                byte[] decodedKey = Base64.getDecoder().decode(encryptedSecretKey);
                byte[] decodedIv = Base64.getDecoder().decode(encryptedIv);

                // Convert the byte arrays back to SecretKey and IvParameterSpec
                this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                this.iv = new IvParameterSpec(decodedIv);

                System.out.println("Secret Key and IV have been retrieved and set for UserSession.");
            } else {
                System.err.println("No encryption key and IV found in the database.");
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving encryption key and IV: " + e.getMessage());
        }
    }
    
    protected void fetchAndSetUserDetails() throws SQLException, Exception {
    
        System.out.println(usersecretKey);
        System.out.println(useriv);
    // Ensure the keys are set before fetching and decrypting user details
    if (usersecretKey == null || useriv == null) {
        throw new Exception("Decryption parameters (secretKey or iv) are missing.");
    }    
    String query = "SELECT first_name, last_name, email_id, phone_number, user_creation_time FROM userinformation WHERE user_id = ?";
    
    try (Connection connection = DriverManager.getConnection(getDatabasePath(userID));
         PreparedStatement preparedStatement = connection.prepareStatement(query)) {

        preparedStatement.setString(1, userID);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            // Decrypt and set first name
            setFirstName(EncryptionUtils.decrypt(resultSet.getString("first_name"), usersecretKey, useriv));

            // Decrypt and set last name
            setLastName(EncryptionUtils.decrypt(resultSet.getString("last_name"), usersecretKey, useriv));

            // Decrypt and set email ID
            setEmailId(EncryptionUtils.decrypt(resultSet.getString("email_id"), usersecretKey, useriv));

            // Decrypt and set phone number
            setPhoneNumber(EncryptionUtils.decrypt(resultSet.getString("phone_number"), usersecretKey, useriv));

            // Decrypt and set user creation time
            String dateTime = EncryptionUtils.decrypt(resultSet.getString("user_creation_time"), usersecretKey, useriv);
            setUserCreationDateTime(dateTime);

        } else {
            System.err.println("User details not found in the database for userID: " + userID);
        }

    } catch (Exception e) {
        System.err.println("Error fetching and decrypting user details: " + e.getMessage());
        throw e;  // Re-throw the exception if necessary
    }
}

       
}
