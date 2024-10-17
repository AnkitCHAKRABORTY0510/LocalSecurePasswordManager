package com.mycompany.securepasswordmanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserDetailsController {

    @FXML
    private PasswordField UserPasswordPasswordField;

    @FXML
    private TextField UserPasswordTextField;

    @FXML
    private ImageView UserPasswordToggle;

    @FXML
    private Label UserNameLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField dateField;

    @FXML
    private TextField timeField;

    @FXML
    private TextField userIDField;
    
    @FXML
    private Button EditDetails;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Get the current session instance
        UserSession session = UserSession.getInstance();
        
        // Set user details from session to UI components
        UserNameLabel.setText(session.getUsername());
        firstNameField.setText(session.getFirstName());
        lastNameField.setText(session.getLastName());
        emailField.setText(session.getEmailId());
        phoneField.setText(session.getPhoneNumber());
        userIDField.setText(session.getUserID());

        // Check if userCreationTime is not null before trying to use it
        if (session.getUserCreationTime() != null) {
            timeField.setText(session.getUserCreationTime().substring(0, 8));  // Keep only HH:mm:ss
        } else {
            timeField.setText("Time not set");
        }

        // Check if userCreationDate is not null before trying to use it
        if (session.getUserCreationDate() != null) {
            dateField.setText(session.getUserCreationDate());
        } else {
            dateField.setText("Date not set");
        }

        // Initialize password fields
        UserPasswordTextField.setText(session.getUserPassword());
        UserPasswordPasswordField.setText(session.getUserPassword());

        // Set initial visibility and manage properties
        UserPasswordTextField.managedProperty().bind(UserPasswordTextField.visibleProperty());
        UserPasswordPasswordField.managedProperty().bind(UserPasswordPasswordField.visibleProperty());

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
            loadImage("Images/passwordshow.png");
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
        // Load image from the classpath
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream != null) {
            UserPasswordToggle.setImage(new Image(imageStream));
        } else {
            System.err.println("Error: Image not found at " + imagePath);
        }
    }

    @FXML
private void openEditUserDetails() {
    try {
        // Load the FXML file for the Edit User Details dialog
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Fxml/EditUserDetails.fxml"));
        DialogPane dialogPane = loader.load();

        // Get the controller for the Edit User Details dialog
        EditUserDetailsController controller = loader.getController();

        // Create a new stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit User Details");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(EditDetails.getScene().getWindow());
        
        // Set the scene with the dialog pane
        Scene scene = new Scene(dialogPane);
        dialogStage.setScene(scene);
        dialogStage.setWidth(700);
        dialogStage.setHeight(500);
        dialogStage.setResizable(false);
        

        // Pass the dialog stage to the controller
        controller.setDialogStage(dialogStage);
        

        // Show the dialog and wait for it to be closed
        dialogStage.showAndWait();
        initialize(); // Re-initialize after dialog is closed


        } catch (IOException e) {
            Logger.getLogger(UserDetailsController.class.getName()).log(Level.SEVERE, null, e);
        }
    }


}
