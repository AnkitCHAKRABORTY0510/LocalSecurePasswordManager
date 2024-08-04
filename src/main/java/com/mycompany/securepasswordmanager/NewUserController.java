package com.mycompany.securepasswordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewUserController {
    @FXML
    private TextField UserNameTextField;

    @FXML
    private PasswordField PasswordTextField;

    @FXML
    private Label CreateUserMessage;

    public void LoginButtonOn(ActionEvent e) {
        if (!UserNameTextField.getText().isBlank() && !PasswordTextField.getText().isBlank()) {
            try {
                if (CreateNewUser(UserNameTextField.getText(), PasswordTextField.getText())) {
                    CreateUserMessage.setText("User created successfully!");
                } else {
                    CreateUserMessage.setText("Username already exists!");
                }
            } catch (SQLException | NoSuchAlgorithmException ex) {
                Logger.getLogger(NewUserController.class.getName()).log(Level.SEVERE, null, ex);
                CreateUserMessage.setText("Error creating user!");
            }
        } else {
            CreateUserMessage.setText("Please enter username and password!");
        }
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

    private boolean CreateNewUser(String username, String password) throws SQLException, NoSuchAlgorithmException {
        if (userExists(username)) {
            return false;
        }

        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        String userID = generateUserID();

        Database.insertUser(username, hashedPassword, salt, userID);
        
        return true;
    }

    private boolean userExists(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeQuery().next();
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    private String generateUserID() {
        return UUID.randomUUID().toString();
    }


}
