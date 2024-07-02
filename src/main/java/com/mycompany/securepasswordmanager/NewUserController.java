/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 *
 * @author mac
 */
    
    
    public class NewUserController {
    
    @FXML
    private TextField UserNameTextField;
    
    @FXML
    private PasswordField PasswordTextField;
    
    @FXML
    private Label CreateUserMessage;
    
    // method to what login button is pressed
    
    public void LoginButtonOn(ActionEvent e)
    {
      
        if (UserNameTextField.getText().isBlank()==false && PasswordTextField.getText().isBlank()==false)
        {
            CreateUserMessage.setText("You try to create user");
            try {
                CreateNewUser();
            } catch (SQLException ex) {
                Logger.getLogger(NewUserController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else
        {
            CreateUserMessage.setText("Please Enter username and password !!");
        }
    }
    
    //function to change between login screen and create 
    //user screen in using goTologin button 
    @FXML
    private void switchToLogin() throws IOException 
    {    
        App.setRoot("login");
    }
    
    
    public boolean CreateNewUser() throws SQLException 
    {
        String username = UserNameTextField.getText();
        String password = PasswordTextField.getText();

        if (username.isBlank() || password.isBlank()) 
        {
            CreateUserMessage.setText("Username and password must not be empty!");
            return false;
        }

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Check if username already exists
        String checkUserExistQuery = "SELECT count(*) FROM UserDetails WHERE UserName = ?";
        String insertUserQuery = "INSERT INTO UserDetails (UserName, UserPasswordHash, Salt) VALUES (?, ?, ?)";

        try 
        {
        // Check if the user already exists
            try (PreparedStatement checkUserExistStatement = connectDB.prepareStatement(checkUserExistQuery)) 
            {
                checkUserExistStatement.setString(1, username);
                try (ResultSet resultSet = checkUserExistStatement.executeQuery()) 
                {
                    if (resultSet.next() && resultSet.getInt(1) > 0) 
                    {
                        CreateUserMessage.setText("Username is already taken. Please choose a different username.");
                        return false;
                    }
                }
            }

            // Generate a salt and hash the password
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            // Insert the new user into the database
            try (PreparedStatement insertUserStatement = connectDB.prepareStatement(insertUserQuery)) 
            {
                insertUserStatement.setString(1, username);
                insertUserStatement.setString(2, hashedPassword);
                insertUserStatement.setString(3, salt);

                int rowsAffected = insertUserStatement.executeUpdate();
                if (rowsAffected > 0) 
                {
                    CreateUserMessage.setText("User created successfully!");
                    return true;
                } 
                else 
                {
                    CreateUserMessage.setText("Failed to create user. Please try again.");
                    return false;
                }
            }
        } 
        catch (SQLException e) 
        {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
            CreateUserMessage.setText("An error occurred during user creation.");
            return false;
        } 
        finally 
        {
            if (connectDB != null) 
            {
                try 
                {
                    connectDB.close();
                }   
                catch (SQLException e) 
                {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    // Utility method to generate a random salt
    private String generateSalt() 
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    // Utility method to hash passwords with a salt
    private String hashPassword(String password, String salt) 
    {
        try 
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedPasswordBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedPasswordBytes);
        } 
        
        catch (NoSuchAlgorithmException e) 
        {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    // Utility method to convert byte array to hex string
    private String bytesToHex(byte[] bytes) 
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) 
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
