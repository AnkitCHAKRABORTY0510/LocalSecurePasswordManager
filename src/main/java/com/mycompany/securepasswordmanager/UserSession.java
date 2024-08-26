package com.mycompany.securepasswordmanager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private String userCreationDateTime;
    private String userCreationDate;
    private String userCreationTime;
    private String userPassword;

    // Fields to store encryption key and IV for encrypting userID for key
    private SecretKey secretKey;
    private IvParameterSpec iv;

    // Fields to store encryption key and IV for decrypting user data
    private SecretKey userSecretKey;
    private IvParameterSpec userIv;

    private static final String DB_URL_PREFIX = "jdbc:sqlite:data/users/";

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

    // Getters and Setters for user encryption keys and IVs
    public SecretKey getUserSecretKey() {
        return userSecretKey;
    }

    public void setUserSecretKey(SecretKey secretKey) {
        System.out.println(secretKey);
        this.userSecretKey = secretKey;
    }

    public IvParameterSpec getUserIv() {
        return userIv;
    }

    public void setUserIv(IvParameterSpec iv) {
        System.out.println(iv);
        this.userIv = iv;
    }

    // Getters and Setters for encryption keys and IVs used for key management
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

    // Getters and Setters for user details
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
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
        this.userCreationDateTime = dateTime;
        this.userCreationDate = dateTime.split("T")[0];
        this.userCreationTime = dateTime.split("T")[1];
    }

    public String getUserCreationDateTime() {
        return userCreationDateTime;
    }

    // Method to initialize the database for a user
    public void initializeDatabase(String userID) {
        setUserID(userID);

        // Ensure the directory structure exists
        Path userDirPath = Paths.get("data", "users");
        File directory = userDirPath.toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Failed to create directory: " + userDirPath.toAbsolutePath());
            return;
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
                                            "account TEXT," +
                                            "salt TEXT NOT NULL," +
                                            "encrypted_password TEXT NOT NULL," +
                                            "description TEXT" +
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

    // Method to fetch and set user details
    protected void fetchAndSetUserDetails() throws SQLException, Exception {
        System.out.println(userSecretKey);
        System.out.println(userIv);

        // Ensure the keys are set before fetching and decrypting user details
        if (userSecretKey == null || userIv == null) {
            throw new Exception("Decryption parameters (secretKey or iv) are missing.");
        }

        String query = "SELECT first_name, last_name, email_id, phone_number, user_creation_time FROM userinformation WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(getDatabasePath(userID));
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Decrypt and set first name
                setFirstName(EncryptionUtils.decrypt(resultSet.getString("first_name"), userSecretKey, userIv));

                // Decrypt and set last name
                setLastName(EncryptionUtils.decrypt(resultSet.getString("last_name"), userSecretKey, userIv));

                // Decrypt and set email ID
                setEmailId(EncryptionUtils.decrypt(resultSet.getString("email_id"), userSecretKey, userIv));

                // Decrypt and set phone number
                setPhoneNumber(EncryptionUtils.decrypt(resultSet.getString("phone_number"), userSecretKey, userIv));

                // Decrypt and set user creation time
                String dateTime = EncryptionUtils.decrypt(resultSet.getString("user_creation_time"), userSecretKey, userIv);
                setUserCreationDateTime(dateTime);

            } else {
                System.err.println("User details not found in the database for userID: " + userID);
            }

        } catch (Exception e) {
            System.err.println("Error fetching and decrypting user details: " + e.getMessage());
            throw e;  // Re-throw the exception if necessary
        }
    }
    
    // Method to update user information in the database based on userID
    protected boolean updateUserInformation(String userName, String firstName, String lastName, String emailId, String phoneNumber) throws Exception {
        String updateSQL = "UPDATE userinformation SET username =?, first_name = ?, last_name = ?, email_id = ?, phone_number = ? WHERE user_id = ?";
        
        // Encrypt the user details before updating
            String encryptedusername = EncryptionUtils.encrypt(userName, userSecretKey, userIv);
            String encryptedFirstName = EncryptionUtils.encrypt(firstName, userSecretKey, userIv);
            String encryptedLastName = EncryptionUtils.encrypt(lastName, userSecretKey, userIv);
            String encryptedEmailId = EncryptionUtils.encrypt(emailId, userSecretKey, userIv);
            String encryptedPhoneNumber = EncryptionUtils.encrypt(phoneNumber, userSecretKey, userIv);
            
        try (Connection connection = DriverManager.getConnection(getDatabasePath(userID));
             PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            
            preparedStatement.setString(1, encryptedusername);
            preparedStatement.setString(2, encryptedFirstName);
            preparedStatement.setString(3, encryptedLastName);
            preparedStatement.setString(4, encryptedEmailId);
            preparedStatement.setString(5, encryptedPhoneNumber);
            preparedStatement.setString(6, userID);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("Error updating user information: " + e.getMessage());
            return false;
        }
    }
}
