///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.mycompany.securepasswordmanager;
//
///**
// *
// * @author mac
// */
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//public class DatabaseConnection {
//    
//  
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/passwordManager"; // Change 'your_database' to your database name
//    private static final String DB_USER = "root"; // Change to your database user
//    private static final String DB_PASSWORD = ""; // Change to your database password
//    private Connection Databaselink;
//    
//    // Method to establish a connection to the database
//    public Connection getConnection() throws SQLException 
//    {
//        Databaselink=DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//        return Databaselink;
//    }
//    
//    // Method to register a new user
//    public boolean registerUser(String UserName, String Password, String Email, String PhoneNo) 
//    {
//        String insertSQL = "INSERT INTO UserDetails (UserName, Password, Email, PhoneNo) VALUES (?, ?, ?, ?)";
//
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
//
//            pstmt.setString(1, UserName);
//            pstmt.setString(2, Password);// Ideally, hash the password before storing
//            pstmt.setString(3, Email);
//            pstmt.setString(4, PhoneNo);
//
//            int rowsAffected = pstmt.executeUpdate();
//            return rowsAffected > 0;
//
//        } catch (SQLException e) {
//            System.out.println("Registration failed: " + e.getMessage());
//            return false;
//        }
//    }
//    
//    // Method to validate login credentials
//    public boolean validateLogin(String UserName, String Password) 
//    {
//        String querySQL = "SELECT password FROM UserDetails WHERE UserName = ?";
//
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
//
//            pstmt.setString(1, UserName);
//            ResultSet rs = pstmt.executeQuery();
//
//            if (rs.next()) {
//                String storedPassword = rs.getString("Password");
//                return Password.equals(storedPassword); // Compare provided password with stored password
//            } else {
//                return false; // User not found
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Login validation failed: " + e.getMessage());
//            return false;
//        }
//    }
//        
//}
//-------------------------------------------------------------------------------------
//package com.mycompany.securepasswordmanager;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//
//public class DatabaseConnection{
//    
//    public Connection databaseLink;
//    
//    public Connection getConnection(){
//        String databaseName = "passwordManager";
//        String databaseUser ="root";
//        String databasePassword= "";
//        String url ="jdbc:mysql://localhost:3306/" + databaseName;
//        
//            try{
//                Class.forName("com.mysql.cj.jdbc.Driver");
//                databaseLink=DriverManager.getConnection(url,databaseUser,databasePassword);
//            }
//            catch(Exception e){
//            e.printStackTrace();
//            }
//    return databaseLink;
//    }
//  }  
//-------------------------------------------------------------------------------------------------

package com.mycompany.securepasswordmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    // Logger for the class
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Database connection parameters as static final fields
//    private static final String DATABASE_NAME = System.getenv("DB_NAME");
//    private static final String DATABASE_USER = System.getenv("DB_USER");
//    private static final String DATABASE_PASSWORD = System.getenv("DB_PASSWORD");
//    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME + "?useSSL=true";

    private static final String DATABASE_NAME = "passwordManager";
    private static final String DATABASE_USER = ("root");
    private static final String DATABASE_PASSWORD =("");
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME + "?useSSL=true";
    private Connection databaseLink;

    public Connection getConnection() {
        try {
            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the database connection
            databaseLink = DriverManager.getConnection(URL, DATABASE_USER, DATABASE_PASSWORD);
        } catch (SQLException e) {
            // Log the SQL exception with a custom message
            LOGGER.log(Level.SEVERE, "Failed to create the database connection.", e);
            throw new DatabaseConnectionException("Failed to connect to the database.", e);
        } catch (ClassNotFoundException e) {
            // Log the ClassNotFoundException if the driver is not found
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found.", e);
            throw new DatabaseConnectionException("MySQL JDBC Driver not found.", e);
        }

        return databaseLink;
    }

    // Custom exception class for database connection errors
    public static class DatabaseConnectionException extends RuntimeException {
        public DatabaseConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
