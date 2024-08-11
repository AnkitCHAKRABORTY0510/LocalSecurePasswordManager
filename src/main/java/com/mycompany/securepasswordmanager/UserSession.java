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
    private String firstName;
    private String lastName;
    private String emailId;
    private String phoneNumber;
    private String userCreationDate;
    private String userCreationTime;
    private String userPassword;//not encrypted

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


    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setUserpassword(String userPassword) {
        this.userPassword = userPassword;//not encripted
    }
    
    public String getUserpassword() {
        return userPassword;//not encripted
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
        this.userCreationDate = dateTime.split("T")[0];
        this.userCreationTime = dateTime.split("T")[1];
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
                                        "user_creation_time TEXT," +
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
    
    public String getDatabasePath(String userID) {
        return DB_URL_PREFIX + userID + ".db";
    }
}
