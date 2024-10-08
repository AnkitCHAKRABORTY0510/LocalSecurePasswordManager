package com.mycompany.securepasswordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class LoginController {

    @FXML
    private TextField UserNameTextField;
    @FXML
    private Label CreateUserMessage;
    @FXML
    private PasswordField PasswordPasswordField;
    @FXML
    private TextField PasswordTextField;
    @FXML
    private ImageView UserPasswordToggle;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Set initial visibility and manage properties
        PasswordTextField.managedProperty().bind(PasswordTextField.visibleProperty());
        PasswordPasswordField.managedProperty().bind(PasswordPasswordField.visibleProperty());

        // Bind text properties between PasswordField and TextField
        PasswordTextField.textProperty().bindBidirectional(PasswordPasswordField.textProperty());

        // Initially hide the TextField (plain text) and show the PasswordField (masked)
        PasswordTextField.setVisible(false);

        // Set up the toggle event listener
        addToggleEventListener();
    }

    // Example usage in the login button action method
    public void LoginButtonOn(ActionEvent e) throws SQLException, NoSuchAlgorithmException, Exception {
        if (!UserNameTextField.getText().isBlank() && !PasswordTextField.getText().isBlank()) {
            if (validateLogin(UserNameTextField.getText(), PasswordTextField.getText())) {
                try {
                    UserSession session = UserSession.getInstance();
                    session.setUserPassword(PasswordTextField.getText());
                    session.setUsername(UserNameTextField.getText());
                    session.setUserID(Database.getUserID(UserNameTextField.getText()));

                    session.initializeDatabase(session.getUserID());

                    // One-time encryption
                    String encryptedUserID = EncryptionUtils.encrypt(session.getUserID(), session.getSecretKey(), session.getIv());
                    UserKeysDatabase.getKeysForUser(encryptedUserID);
                    
                    session.setUserSecretKey(UserKeysDatabase.DatasecretKey);
                    session.setUserIv(UserKeysDatabase.Dataiv);
                    session.fetchAndSetUserDetails(); // Encrypt remaining details

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

    private boolean validateLogin(String username, String password) throws SQLException, NoSuchAlgorithmException, Exception {
        return Database.validateLogin(username, password);
    }

    @FXML
    private void switchToNewUser() throws IOException {
        App.setRoot("NewUser");
    }

    @FXML
    private void switchToMainScreen() throws IOException {
        App.setRoot("MainView", 1325, 800);
    }

    @FXML
    private void handleTogglePasswordVisibility(MouseEvent event) {
        // Toggle visibility between PasswordField and TextField
        if (isPasswordVisible) {
            // Hide password (show PasswordField, hide TextField)
            PasswordTextField.setVisible(false);
            PasswordPasswordField.setVisible(true);
            loadImage("Images/passwordshowlogin.png"); // Path adjusted for resource loading
        } else {
            // Show password (hide PasswordField, show TextField)
            PasswordTextField.setVisible(true);
            PasswordPasswordField.setVisible(false);
            loadImage("Images/passwordhidelogin.png"); // Path adjusted for resource loading
        }
        // Toggle the state
        isPasswordVisible = !isPasswordVisible;
    }

    private void addToggleEventListener() {
        // Add click listener to the ImageView (toggle button)
        UserPasswordToggle.setOnMouseClicked(this::handleTogglePasswordVisibility);
    }

    private void loadImage(String imagePath) {
    try {
        // Load the image using getClass().getResource()
        // This ensures the path works across both Windows and Linux
        URL imageURL = getClass().getResource(imagePath);
        
        if (imageURL != null) {
            // Image found, load it
            UserPasswordToggle.setImage(new Image(imageURL.toString()));
            System.out.println("Loading image from: " + imagePath);
        } else {
            // Image not found, handle the error
            System.err.println("Error: Image not found at " + imagePath);
        }
    } catch (Exception e) {
        // Log and handle unexpected errors during image loading
        System.err.println("Exception while loading image: " + e.getMessage());
        e.printStackTrace();
    }
}

    }

