package com.mycompany.securepasswordmanager;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:data/password_manager.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            File dbDir = new File("data");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " username TEXT NOT NULL,\n"
                + " password TEXT NOT NULL,\n"
                + " salt TEXT NOT NULL,\n"
                + " user_id TEXT UNIQUE NOT NULL\n"
                + ");";

        

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean validateLogin(String username, String password) throws SQLException, NoSuchAlgorithmException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String hashedPassword = NewUserController.hashPassword(password, salt);
                return storedPassword.equals(hashedPassword);
            }
        }
        return false;
    }

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
            System.out.println(e.getMessage());
        }
    }

    public static String getUserID(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("user_id");
            }
        }
        return null;
    }


}
