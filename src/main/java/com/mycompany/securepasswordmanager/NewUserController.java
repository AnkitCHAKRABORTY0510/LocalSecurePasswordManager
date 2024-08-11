package com.mycompany.securepasswordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class NewUserController {

    @FXML
    private TextField UserNameTextField;
    @FXML
    private TextField PasswordTextField;
    @FXML
    private TextField FirstNameTextField;
    @FXML
    private TextField LastNameTextField;
    @FXML
    private TextField EmailTextField;
    @FXML
    private TextField PhoneNoTextField;
    @FXML
    private Label CreateUserMessage;
    @FXML
    private PasswordField PasswordPasswordField;
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
    
    public void CreateUserButtonOn(ActionEvent e) {
        if (!UserNameTextField.getText().isBlank() && !PasswordTextField.getText().isBlank()) {
            try {
                if (CreateNewUser(UserNameTextField.getText(), PasswordTextField.getText(), FirstNameTextField.getText(),
                        LastNameTextField.getText(), EmailTextField.getText(), PhoneNoTextField.getText())) {
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

    private boolean CreateNewUser(String username, String password, String firstName, String lastName, String email, String phoneNo) throws SQLException, NoSuchAlgorithmException {
        if (userExists(username)) {
            return false;
        }

        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        String userID = generateUserID();

        // Insert into main database
        Database.insertUser(username, hashedPassword, salt, userID);

        // Insert additional details into user-specific database
        UserSession userSession = UserSession.getInstance();
        userSession.setUserID(userID);
        userSession.initializeDatabase();
        insertUserDetailsToUserDatabase(username, hashedPassword, userID, firstName, lastName, email, phoneNo);

        return true;
    }

    private void insertUserDetailsToUserDatabase(String username, String hashedPassword, String userID, String firstName, String lastName, String email, String phoneNo) throws SQLException {
        String sql = "INSERT INTO userinformation (username, user_id, first_name, last_name, email_id, phone_number, user_creation_time) VALUES(?,?,?,?,?,?,?)";
        String dbUrl = UserSession.getInstance().getUserID(); // Assuming it returns the correct DB path

        try (Connection conn = DriverManager.getConnection(UserSession.getInstance().getDatabasePath(dbUrl));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, userID);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setString(6, phoneNo);
            pstmt.setString(7, getCurrentTimestamp());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean userExists(String username) throws SQLException {
        return Database.userExists(username);
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

    private String getCurrentTimestamp() {
        return java.time.LocalDateTime.now().toString();
    }
    
    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("Login");
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
