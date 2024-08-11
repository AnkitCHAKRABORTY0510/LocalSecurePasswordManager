package com.mycompany.securepasswordmanager;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.util.Duration;

public class AddNewPasswordDialogController {

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TextField urlField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button GenerateRandomPassword;

    @FXML
    private Button ADD;

    @FXML
    private Button CANCEL;

    private Dialog<Void> dialog;

    private int passwordLength = 15; // Default password length
    @FXML
    private AnchorPane OK;
    @FXML
    private Label currentPassword;
    @FXML
    private Label currentPassword1;
    @FXML
    private TextField passwordField1;
    @FXML
    private TextField urlField1;

    public void setDialog(Dialog<Void> dialog) {
        this.dialog = dialog;
    }

    public void initialize() {
        if (ADD != null) {
            ADD.setOnAction(event -> {
                handleOK();
            });
        }

        if (CANCEL != null) {
            CANCEL.setOnAction(event -> {
                handleCANCEL();
            });
        }

        GenerateRandomPassword.setOnAction(event -> {
            PasswordData generatedPassword = PasswordGenerator.generatePassword(passwordLength);
            if (generatedPassword != null) {
                String decryptedPassword = decryptPassword(generatedPassword);//decrypt the genenrated password before showing
                passwordField.setText(decryptedPassword);
            } else {
                showAlert("Error", "Failed to generate password.");
            }
        });
    }

    private void handleOK() {
        if (validateInput()) {
            preprocessingPassword();
            closeDialog();
        }
    }

    private void handleCANCEL() {
        closeDialog();
    }

    private boolean validateInput() {
        String url = urlField.getText();
        String password = passwordField.getText();
        String description = descriptionArea.getText();

        if (url == null || url.trim().isEmpty()) {
            showAlert("Validation Error", "URL cannot be empty.");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Password cannot be empty.");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.close();
    }
    
    
    //decrypt the generate password based on the data encripted data provided
    private String decryptPassword(PasswordData passwordData) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(passwordData.getSalt());
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            byte[] encryptedBytes = Base64.getDecoder().decode(passwordData.getEncryptedPassword());

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, originalKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to decrypt password.");
            return "";
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        // Set the owner of the alert to the main application window
        Stage ownerStage = (Stage) ADD.getScene().getWindow(); // Replace 'someNode' with a node in your current scene
        alert.initOwner(ownerStage);

        // Set modality to ensure the alert is modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Optionally, set the preferred size
        dialogPane.setPrefSize(300, 120); // Adjust as needed
        
          // Create a Timeline that will close the alert after 10 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> alert.close()));
        timeline.setCycleCount(1);
        timeline.play();
        alert.showAndWait();
        
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private String encryptPassword(String password, byte[] salt) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(salt);
            keyGen.init(128, secureRandom);
            SecretKey secretKey = keyGen.generateKey();

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());

            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);

            return encodedSalt + ":" + encryptedPassword;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to encrypt password.");
            return null;
        }
    }

    private void preprocessingPassword() {
        String url = urlField.getText();
        String originalPassword = passwordField.getText();
        String description = descriptionArea.getText();
        byte[] salt = generateSalt();
        String encryptedPasswordWithSalt = encryptPassword(originalPassword, salt);
        if (encryptedPasswordWithSalt != null) {
            storePasswordInDatabase(url, encryptedPasswordWithSalt, description);
        }
    }

    private void storePasswordInDatabase(String url, String encryptedPasswordWithSalt, String description) {
        String[] parts = encryptedPasswordWithSalt.split(":");
        String salt = parts[0];
        String encryptedPassword = parts[1];

        UserSession userSession = UserSession.getInstance();
        String dbUrl = "jdbc:sqlite:Data/Users/" + userSession.getUserID() + ".db";
        String insertSQL = "INSERT INTO passwords (url, salt, encrypted_password, description, username) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(dbUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, url);
            preparedStatement.setString(2, salt);
            preparedStatement.setString(3, encryptedPassword);
            preparedStatement.setString(4, description);
            preparedStatement.setString(5, userSession.getUsername());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to store password in database.");
        }
    }
}
