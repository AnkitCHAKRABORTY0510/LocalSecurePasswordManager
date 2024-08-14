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
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

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
    
    private SecretKey ankit;
    private IvParameterSpec chakraborty;
 
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
    
    private boolean isValidEmail(String email) {
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + 
                        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    return email.matches(emailRegex);
    }
    
    public  void CreateUserButtonOn(ActionEvent e) throws Exception {
    if (!UserNameTextField.getText().isBlank() && !PasswordTextField.getText().isBlank()) {
        if (!isValidEmail(EmailTextField.getText())) {
            CreateUserMessage.setText("Invalid email address!");
            return;
        }
        
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
    
    private boolean CreateNewUser(String userName, String password, String firstName, String lastName, String email, String phoneNo) throws Exception {
        if (userExists(userName)) {
            return false;
        }
        
        UserKeysDatabase.createKeysTable();
        // Hash the password with a salt
        String salt = SecurityUtils.generateSalt();
        String hashedPassword = SecurityUtils.hashData(password, salt);
        
        
        // Encrypt user details
        UserSession userSession = UserSession.getInstance();
//        userSession.setSecretKey(EncryptionUtils.generateSecretKey());
//        userSession.setIv(EncryptionUtils.generateIv());
        userSession.setUserCreationDateTime(getCurrentTimestamp());
        
        
        //generate userid
        String UserID = generateUserID();        
        userSession.setUserID(UserID);
        
        SecretKey userSecretKey = EncryptionUtils.generateSecretKey();
        IvParameterSpec userIv= EncryptionUtils.generateIv();
        
        userSession.setuserSecretKey(userSecretKey);
        userSession.setuserIv(userIv);
        //store userid and data secret & IV into keys.db
        storeEncryptedUserIDAndKeys(UserID, userIv, userSecretKey);
    
        userSession.setSecretKey(ankit);
        userSession.setIv(chakraborty);
        
        
        
        String encryptedFirstName = EncryptionUtils.encrypt(firstName, userSession.getuserSecretKey(), userSession.getuserIv());
        String encryptedLastName = EncryptionUtils.encrypt(lastName, userSession.getuserSecretKey(), userSession.getuserIv());
        String encryptedEmail = EncryptionUtils.encrypt(email, userSession.getuserSecretKey(), userSession.getuserIv());
        String encryptedPhoneNo = EncryptionUtils.encrypt(phoneNo, userSession.getuserSecretKey(), userSession.getuserIv());
        userSession.setUserCreationDateTime(getCurrentTimestamp());
        String UserCreationDateTime = EncryptionUtils.encrypt(userSession.getUserCreationDateTime(), userSession.getuserSecretKey(), userSession.getuserIv());
        String encryptedUsername = EncryptionUtils.encrypt(userName, userSession.getuserSecretKey(), userSession.getuserIv());

        String encryptUsername = Database.encryptUsername(userName);//common database//using database secrete key
        // Insert into the database
        Database.insertUser(encryptUsername, hashedPassword, salt, UserID);
        
        
        userSession.initializeDatabase(UserID);
        storeKeysInDatabase(ankit,chakraborty);
        
        

        // Insert additional user details        
        insertUserDetailsToUserDatabase(encryptedUsername, encryptedFirstName, encryptedLastName, encryptedEmail, encryptedPhoneNo, UserCreationDateTime,UserID);//username for user specific database.
        UserSession.getInstance().logout();
        return true;
    }

    private void insertUserDetailsToUserDatabase(String encryptedUsername, String encryptedFirstName, String encryptedLastName, String encryptedEmail, String encryptedPhoneNo, String UserCreationDateTime, String UserID) throws SQLException {
        // Insert user details into the user-specific database
        String sql = "INSERT INTO userinformation (username, first_name, last_name, user_creation_time, email_id, phone_number, user_id) VALUES(?,?,?,?,?,?,?)";
        String dbUrl = UserSession.getInstance().getUserID(); // Assuming it returns the correct DB path
        
        try (Connection conn = DriverManager.getConnection(UserSession.getInstance().getDatabasePath(dbUrl));
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encryptedUsername);
            pstmt.setString(2, encryptedFirstName);
            pstmt.setString(3, encryptedLastName);
            pstmt.setString(4, UserCreationDateTime);
            pstmt.setString(5, encryptedEmail);
            pstmt.setString(6, encryptedPhoneNo);
            pstmt.setString(7, UserID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    //store in each user database
    private void storeKeysInDatabase(SecretKey secretKey, IvParameterSpec iv) {
        // Convert SecretKey and IvParameterSpec to Base64 encoded strings
        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        String encodedIv = Base64.getEncoder().encodeToString(iv.getIV());

        // SQL to insert the encoded secret key and IV into the encryption_keys table
        String sql = "INSERT INTO encryption_keys (encrypted_secret_key, encrypted_iv) VALUES(?, ?)";

        // Get the user-specific database path
        String dbUrl = UserSession.getInstance().getUserID();

        try (Connection conn = DriverManager.getConnection(UserSession.getInstance().getDatabasePath(dbUrl));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the encoded values into the SQL statement
            pstmt.setString(1, encodedSecretKey);
            pstmt.setString(2, encodedIv);

            // Execute the update
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error storing keys: " + e.getMessage());
        }
    }

    
    
    private void storeEncryptedUserIDAndKeys(String userID, IvParameterSpec userIV, SecretKey userSecretKey) throws Exception {
        // Generate a new secret key and IV
        this.ankit = EncryptionUtils.generateSecretKey();
        this.chakraborty = EncryptionUtils.generateIv();

        // Encrypt the userID
        String encryptedUserID = EncryptionUtils.encrypt(userID, this.ankit, this.chakraborty);

        // Convert SecretKey and IvParameterSpec to Base64 encoded strings
        String encodedSecretKey = Base64.getEncoder().encodeToString(userSecretKey.getEncoded());
        String encodedIv = Base64.getEncoder().encodeToString(userIV.getIV());

        // Store the encrypted userID, secret key, and IV in the third table of the user-specific database
        String sql = "INSERT INTO encryption_keys (encrypted_user_id, secret_key, iv) VALUES(?, ?, ?)";
        
        System.out.println("hoho1");
        
        
        
        try (Connection conn = DriverManager.getConnection(UserKeysDatabase.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encryptedUserID);
            pstmt.setString(2, encodedSecretKey);
            pstmt.setString(3, encodedIv);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("hoho2");
            System.out.println(e.getMessage());
        }
        System.out.println("hoho3");
    }
    
    private boolean userExists(String username) throws SQLException, Exception {
        return Database.userExists(username);
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
