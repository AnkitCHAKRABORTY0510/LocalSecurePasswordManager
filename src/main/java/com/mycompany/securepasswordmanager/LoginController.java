package com.mycompany.securepasswordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    @FXML
    private TextField UserNameTextField;

    @FXML
    private PasswordField PasswordTextField;

    @FXML
    private Label CreateUserMessage;

    public void LoginButtonOn(ActionEvent e) throws SQLException, NoSuchAlgorithmException {
        if (!UserNameTextField.getText().isBlank() && !PasswordTextField.getText().isBlank()) {
            if (validateLogin(UserNameTextField.getText(), PasswordTextField.getText())) {
                try {
                    UserSession session = UserSession.getInstance();
                    session.setUsername(UserNameTextField.getText());
                    session.setUserID(Database.getUserID(UserNameTextField.getText()));
                    session.initializeDatabase();
                    
                    switchToMainScreen();
                } catch (IOException ex) {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                CreateUserMessage.setText("Invalid username or password!");
            }
        } else {
            CreateUserMessage.setText("Please Enter username and password!!");
        }
    }

    private boolean validateLogin(String username, String password) throws SQLException, NoSuchAlgorithmException {
        return Database.validateLogin(username, password);
    }

    

    @FXML
    private void switchToNewUser() throws IOException {
        App.setRoot("NewUser");
    }

    @FXML
    private void switchToMainScreen() throws IOException {
        App.setRoot("MainView", 1024, 768);
    }
}
