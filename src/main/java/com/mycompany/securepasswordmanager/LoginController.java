package com.mycompany.securepasswordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
    
    public void fetchAndSetUserDetails(String userID) throws SQLException {
        UserSession session = UserSession.getInstance();

        String query = "SELECT first_name, last_name, email_id, phone_number, user_creation_time, user_id FROM userinformation WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(session.getDatabasePath(userID));
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                session.setFirstName(resultSet.getString("first_name"));
                session.setLastName(resultSet.getString("last_name"));
                session.setEmailId(resultSet.getString("email_id"));
                session.setPhoneNumber(resultSet.getString("phone_number"));

                // Assuming user_creation_time is stored as 'YYYY-MM-DDTHH:MM:SS'
                String dateTime = resultSet.getString("user_creation_time");
                session.setUserCreationDateTime(dateTime);

                session.setUserID(resultSet.getString("user_id"));
            } else {
                System.err.println("User details not found in the database for userID: " + userID);
            }

        }    }

    // Example usage in the login button action method
    public void LoginButtonOn(ActionEvent e) throws SQLException, NoSuchAlgorithmException {
        if (!UserNameTextField.getText().isBlank() && !PasswordTextField.getText().isBlank()) {
            if (validateLogin(UserNameTextField.getText(), PasswordTextField.getText())) {
                try {
                    UserSession session = UserSession.getInstance();
                    session.setUsername(UserNameTextField.getText());
                    session.setUserID(Database.getUserID(UserNameTextField.getText()));
                    session.setUserpassword(PasswordTextField.getText());

                    // Fetch and set user details
                    fetchAndSetUserDetails(session.getUserID());

                    // Optionally initialize the user's database
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
        App.setRoot("MainView", 1200, 800);
    }
    
    @FXML
    private void handleTogglePasswordVisibility(MouseEvent event) {
        // Toggle visibility between PasswordField and TextField
       
        if (isPasswordVisible) {
            // Hide password (show PasswordField, hide TextField)
            PasswordTextField.setVisible(false);
            PasswordPasswordField.setVisible(true);
            loadImage("Images/passwordshowlogin.png"+ "");
        } else {
            // Show password (hide PasswordField, show TextField)
            PasswordTextField.setVisible(true);
            PasswordPasswordField.setVisible(false);
            loadImage("Images/passwordhidelogin.png");
        }
        // Toggle the state
        isPasswordVisible = !isPasswordVisible;
    }
    
    private void addToggleEventListener() {
        
        // Add click listener to the ImageView (toggle button)
        UserPasswordToggle.setOnMouseClicked(this::handleTogglePasswordVisibility);
        
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
