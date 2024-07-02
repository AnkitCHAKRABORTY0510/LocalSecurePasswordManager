/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author mac
 */
public class LoginController {
    @FXML
    private TextField UserNameTextField;
    
    @FXML
    private PasswordField PasswordTextField;
    
    @FXML
    private Label CreateUserMessage;
    
    // method to what login button is pressed
    
    public void LoginButtonOn(ActionEvent e) throws SQLException
    {
      
        if (UserNameTextField.getText().isBlank()==false && PasswordTextField.getText().isBlank()==false)
        {
            CreateUserMessage.setText("You try to create user");
            if (ValidateLogin())
                try {
                    switchToMainScreen();
            } catch (IOException ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            CreateUserMessage.setText("Please Enter username and password !!");
        }
    }
    
       @FXML
    private void switchToNewUser() throws IOException 
    {
        App.setRoot("NewUser");
    }
    
    public boolean ValidateLogin() throws SQLException       
    {

        String username = UserNameTextField.getText();
        String password = PasswordTextField.getText();

        // Validate input
        if (username.isBlank() || password.isBlank()) 
        {
            CreateUserMessage.setText("Please enter username and password!");
            return false;
        }

        // Establish connection to the database
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // SQL query to retrieve the stored hash and salt
        String query = "SELECT UserPasswordHash, Salt FROM UserDetails WHERE UserName = ?";

        try (PreparedStatement preparedStatement = connectDB.prepareStatement(query)) 
        {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) 
            {
                if (resultSet.next()) 
                {
                    String storedHash = resultSet.getString("UserPasswordHash");
                    String storedSalt = resultSet.getString("Salt");

                    // Hash the input password with the retrieved salt
                        String inputHash = hashPassword(password, storedSalt);

                    // Compare the stored hash with the hashed input password
                    if (storedHash.equals(inputHash)) 
                    {
                        CreateUserMessage.setText("Welcome");
                        return true;
                    } 

                    else 
                    {
                        CreateUserMessage.setText("Invalid Login!");
                        return false;
                    }
                } 

                else 
                {
                    CreateUserMessage.setText("Invalid Login!");
                    return false;
                }
            }
        } 
        catch (SQLException e) 
        {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
            CreateUserMessage.setText("An error occurred during login.");
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
    
    //utility method to hashPassword
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
        for (byte b : bytes) {
        sb.append(String.format("%02x", b));
    }
        return sb.toString();
}
    
    
    // function to switch to main screen
    @FXML
    private void switchToMainScreen() throws IOException {   
        App.setRoot("MainScreen");
    }   
}
