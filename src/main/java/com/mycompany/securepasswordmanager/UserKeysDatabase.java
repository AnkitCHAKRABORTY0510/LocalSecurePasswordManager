package com.mycompany.securepasswordmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

public class UserKeysDatabase {

    // Define the database URL
    protected static String DB_URL = "jdbc:sqlite:data/keys/keys.db";
    
    // Static fields to store the secret key and IV
    protected static SecretKey DatasecretKey;
    protected static IvParameterSpec Dataiv;

    // Getter for DatasecretKey
    public static SecretKey getDatasecretKey() {
        return DatasecretKey;
    }

    // Setter for DatasecretKey
    public static void setDatasecretKey(SecretKey datasecretKey) {
        DatasecretKey = datasecretKey;
    }

    // Getter for Dataiv
    public static IvParameterSpec getDataiv() {
        return Dataiv;
    }

    // Setter for Dataiv
    public static void setDataiv(IvParameterSpec dataiv) {
        Dataiv = dataiv;
    }

    // Method to connect to the database
    protected static Connection connectkey() {
        Connection conn = null;
        try {
            // Ensure the directory structure exists
            File directory = new File("data/keys");
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new SQLException("Failed to create directory: data/keys");
                }
            }

            // Establish a connection to the database
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connection to the keys database has been established.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return conn;
    }

    // Method to create the encryption_keys table if it doesn't exist
    protected static void createKeysTable() {
        String createKeysTableSQL = "CREATE TABLE IF NOT EXISTS encryption_keys (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "encrypted_user_id TEXT UNIQUE NOT NULL," +
                                    "secret_key TEXT NOT NULL," +
                                    "iv TEXT NOT NULL" +
                                    ");";
        try (Connection conn = connectkey();
             Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(createKeysTableSQL);
                System.out.println("encryption_keys table created or already exists.");
            }
        } catch (SQLException e) {
            System.err.println("Error creating encryption_keys table: " + e.getMessage());
        }
    }

    // Method to retrieve the SecretKey for a given encrypted userID
    protected static SecretKey getSecretKey(String encryptedUserID) throws SQLException {
        String sql = "SELECT secret_key FROM encryption_keys WHERE encrypted_user_id = ?";
        try (Connection conn = connectkey();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn != null) {
                pstmt.setString(1, encryptedUserID);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String secretKeyEncoded = rs.getString("secret_key");
                    byte[] decodedKey = Base64.getDecoder().decode(secretKeyEncoded);
                    return new SecretKeySpec(decodedKey, EncryptionUtils.AES);
                }
            }
        }
        return null;
    }

    // Method to retrieve the IvParameterSpec for a given encrypted userID
    protected static IvParameterSpec getIv(String encryptedUserID) throws SQLException {
        String sql = "SELECT iv FROM encryption_keys WHERE encrypted_user_id = ?";
        try (Connection conn = connectkey();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn != null) {
                pstmt.setString(1, encryptedUserID);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String ivEncoded = rs.getString("iv");
                    byte[] decodedIv = Base64.getDecoder().decode(ivEncoded);
                    return new IvParameterSpec(decodedIv);
                }
            }
        }
        return null;
    }

    // Static method to retrieve both SecretKey and IvParameterSpec for a given encrypted userID
    protected static void getKeysForUser(String encryptedUserID) throws SQLException {
        String sql = "SELECT secret_key, iv FROM encryption_keys WHERE encrypted_user_id = ?";
        try (Connection conn = connectkey();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn != null) {
                pstmt.setString(1, encryptedUserID);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // Retrieve and decode the secret key
                    String secretKeyEncoded = rs.getString("secret_key");
                    System.out.println(secretKeyEncoded);
                    
                    byte[] decodedKey = Base64.getDecoder().decode(secretKeyEncoded);
                    DatasecretKey = new SecretKeySpec(decodedKey, EncryptionUtils.AES);

                    // Retrieve and decode the IV
                    String ivEncoded = rs.getString("iv");
                    System.out.println(ivEncoded);
                    byte[] decodedIv = Base64.getDecoder().decode(ivEncoded);
                    Dataiv = new IvParameterSpec(decodedIv);
                }
            }
        }
    }
    
    
}
