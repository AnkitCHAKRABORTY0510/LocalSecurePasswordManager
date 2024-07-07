/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.securepasswordmanager;



import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class UserDetailsController {

    @FXML
    private PasswordField UserPasswordPasswordField;

    @FXML
    private TextField UserPasswordTextField;

    @FXML
    private ImageView UserPasswordToggle;

    private boolean isPasswordVisible = false;
    
    @FXML
    private Label UsrDelMsg;

    @FXML
    public void initialize() {
        
        // Set initial visibility and manage properties
        UserPasswordTextField.managedProperty().bind(UserPasswordTextField.visibleProperty());
        UserPasswordPasswordField.managedProperty().bind(UserPasswordPasswordField.visibleProperty());

        // Bind text properties between PasswordField and TextField
        UserPasswordTextField.textProperty().bindBidirectional(UserPasswordPasswordField.textProperty());

        // Initially hide the TextField (plain text) and show the PasswordField (masked)
        UserPasswordTextField.setVisible(false);

        // Set up the toggle event listener
        addToggleEventListener();
    }

    private void addToggleEventListener() {
        
        // Add click listener to the ImageView (toggle button)
        UserPasswordToggle.setOnMouseClicked(this::handleTogglePasswordVisibility);
    }

   @FXML
    private void handleTogglePasswordVisibility(MouseEvent event) {
        // Toggle visibility between PasswordField and TextField
        if (isPasswordVisible) {
            // Hide password (show PasswordField, hide TextField)
            UserPasswordTextField.setVisible(false);
            UserPasswordPasswordField.setVisible(true);
            loadImage("Images/passwordshow.png"+ "");
        } else {
            // Show password (hide PasswordField, show TextField)
            UserPasswordTextField.setVisible(true);
            UserPasswordPasswordField.setVisible(false);
            loadImage("Images/passwordhide.png");
        }
        // Toggle the state
        isPasswordVisible = !isPasswordVisible;
    }

    private void loadImage(String imagePath) {
        // Detailed debugging to ensure image loading
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            System.err.println("Error: Image not found at " + imagePath);
        } else {
            System.out.println("Loading image from: " + imagePath);
            UserPasswordToggle.setImage(new Image(imageStream));
        }
    }
}