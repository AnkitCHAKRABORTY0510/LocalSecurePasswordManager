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

import javafx.scene.layout.AnchorPane;

public class EditPasswordDialogController {

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TextField urlField;

    @FXML
    private TextField passwordField;
    
    @FXML
    private TextField CurrentUrlField;

    @FXML
    private TextField CurrentPasswordField;


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
   
    private PasswordData passwordData;

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
            urlField.setText(CurrentUrlField.getText());
            return true;
        }

        if (password == null || password.trim().isEmpty()) {
            passwordField.setText(CurrentPasswordField.getText());
            return true;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.close();
    }
    
    public void setPasswordData(PasswordData passwordData) {
        this.passwordData = passwordData;
        CurrentUrlField.setText(passwordData.getUrl());
        CurrentPasswordField.setText(passwordData.getEncryptedPassword());
        descriptionArea.setText(passwordData.getDescription());
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
            savePassword(url, encryptedPasswordWithSalt, description);
        }
    }

    private void savePassword(String newUrl, String encryptedPasswordWithSalt, String newDescription) {
        
        String[] parts = encryptedPasswordWithSalt.split(":");
        String salt = parts[0];
        String newPassword = parts[1];
        
        UserSession userSession = UserSession.getInstance();
        String dbUrl = "jdbc:sqlite:data/users/" + userSession.getUserID() + ".db";
        String updateSQL = "UPDATE passwords SET url = ?, encrypted_password = ?, salt = ?, description = ? WHERE id = ?";
        
        try (Connection connection = DriverManager.getConnection(dbUrl);
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {

            statement.setString(1, newUrl);
            statement.setString(2, newPassword);
            statement.setString(3, salt);
            statement.setString(4, newDescription);
            statement.setInt(5, passwordData.getId());

            statement.executeUpdate();

            closeDialog();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update password.");
        }
    }

}