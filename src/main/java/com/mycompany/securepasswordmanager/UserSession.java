package com.mycompany.securepasswordmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String userID;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void initializeDatabase() {
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
                                        "last_login_time TEXT," +
                                        "date_of_birth TEXT," +
                                        "email_id TEXT," +
                                        "phone_number TEXT," +
                                        "user_id TEXT UNIQUE" +
                                        ")";

            String createPasswordTableSQL = "CREATE TABLE IF NOT EXISTS passwords (" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "url TEXT NOT NULL," +
                                            "salt TEXT NOT NULL," +
                                            "encrypted_password TEXT NOT NULL," +
                                            "description TEXT," +
                                            "username TEXT NOT NULL," +
                                            "FOREIGN KEY(username) REFERENCES userinformation(username)" +
                                            ")";

            statement.executeUpdate(createUserTableSQL);
            statement.executeUpdate(createPasswordTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
