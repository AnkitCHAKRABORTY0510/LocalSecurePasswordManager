package com.mycompany.securepasswordmanager;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Database {

    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data";
    private static final String DB_URL = "jdbc:sqlite:" + DATA_DIR + File.separator + "password_manager.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            // Create the directory if it doesn't exist
            File dbDir = new File(DATA_DIR);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
        return conn;
    }

    // Creates the necessary tables in the database
    public static void createNewTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " username TEXT NOT NULL,\n"
                + " password TEXT NOT NULL,\n"
                + " salt TEXT NOT NULL,\n"
                + " user_id TEXT UNIQUE NOT NULL\n"
                + ");";

        String createEncryptionKeysTable = "CREATE TABLE IF NOT EXISTS encryption_keys (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " secret_key TEXT NOT NULL,\n"
                + " iv TEXT NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createEncryptionKeysTable);

            // Generate and store the key and IV
            try {
                generateAndStoreKey();
            } catch (Exception ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, "Error generating/storing key: ", ex);
            }

        } catch (SQLException e) {
            System.out.println("Failed to create tables: " + e.getMessage());
        }
    }

    // Inserts a new user into the users table
    public static void insertUser(String username, String password, String salt, String userID) {
        String sql = "INSERT INTO users(username, password, salt, user_id) VALUES(?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, salt);
            pstmt.setString(4, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to insert user: " + e.getMessage());
        }
    }

    // Inserts the secret key and IV into the encryption_keys table
    protected static void insertEncryptionKeys(String secretKey, String iv) {
        String sql = "INSERT INTO encryption_keys(secret_key, iv) VALUES(?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, secretKey);
            pstmt.setString(2, iv);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to insert encryption keys: " + e.getMessage());
        }
    }

    // Retrieves the secret key from the encryption_keys table
    protected static String getSecretKey() throws SQLException {
        String sql = "SELECT secret_key FROM encryption_keys LIMIT 1";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("\n\nsecret_key " + rs.getString("secret_key"));
                return rs.getString("secret_key");
            }
        }
        return null;
    }

    // Retrieves the IV from the encryption_keys table
    protected static String getIv() throws SQLException {
        String sql = "SELECT iv FROM encryption_keys LIMIT 1";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("IVkey " + rs.getString("iv"));
                return rs.getString("iv");
            }
        }
        return null;
    }

    // Encrypts the username using the stored secret key and IV
    protected static String encryptUsername(String username) throws Exception {
        String secretKeyEncoded = getSecretKey();
        String ivEncoded = getIv();

        if (secretKeyEncoded == null || ivEncoded == null) {
            throw new Exception("Secret key or IV is not available in the database.");
        }

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKeyEncoded), EncryptionUtils.AES);
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivEncoded));

        return EncryptionUtils.encrypt(username, secretKey, iv);
    }

    // Decrypts the username using the stored secret key and IV
    protected static String decryptUsername(String encryptedUsername) throws Exception {
        String secretKeyEncoded = getSecretKey();
        String ivEncoded = getIv();

        if (secretKeyEncoded == null || ivEncoded == null) {
            throw new Exception("Secret key or IV is not available in the database.");
        }

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKeyEncoded), EncryptionUtils.AES);
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivEncoded));

        return EncryptionUtils.decrypt(encryptedUsername, secretKey, iv);
    }

    public static boolean userExists(String username) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ?";

        String encryptedUsername = encryptUsername(username);

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encryptedUsername);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Failed to check if user exists: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateLogin(String username, String password) throws SQLException, NoSuchAlgorithmException, Exception {
        String sql = "SELECT * FROM users WHERE username = ?";

        String encryptedUsername = encryptUsername(username);
        System.out.println("encryptedusename: " + encryptedUsername);
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encryptedUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String hashedPassword = SecurityUtils.hashData(password, salt);
                System.out.println("hashedPassword :"+hashedPassword);
                return storedPassword.equals(hashedPassword);
            }
        }
        return false;
    }

    public static String getUserID(String username) throws SQLException, Exception {
        String sql = "SELECT user_id FROM users WHERE username = ?";

        String encryptedUsername = encryptUsername(username);

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encryptedUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("user_id");
            }
        }
        return null;
    }

    protected static void generateAndStoreKey() throws Exception {
        // Generate and store the secret key and IV
        SecretKey secretKey = EncryptionUtils.generateSecretKey();
        IvParameterSpec iv = EncryptionUtils.generateIv();

        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        String encodedIv = Base64.getEncoder().encodeToString(iv.getIV());

        Database.insertEncryptionKeys(encodedSecretKey, encodedIv);
    }
    
    /**
     * Updates user details in the users table based on the provided user ID.
     *
     * @param userID The unique ID of the user to update.
     * @param username The new username (encrypted) for the user.
     * @param password The new hashed password for the user.
     * @param salt The new salt used for hashing the password.
     * @return true if the update was successful, false otherwise.
     */
    protected static boolean updateUserDetails(String userID, String username, String password, String salt) {
        String sql = "UPDATE users SET username = ?, password = ?, salt = ? WHERE user_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Encrypt the username before storing it
            

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, salt);
            pstmt.setString(4, userID);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Failed to update user details: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Failed to encrypt username: " + e.getMessage());
            return false;
        }
    }

    
}
