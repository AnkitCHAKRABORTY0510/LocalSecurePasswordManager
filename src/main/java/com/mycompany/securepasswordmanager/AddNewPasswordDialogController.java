package com.mycompany.securepasswordmanager;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import java.util.Base64;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
    
    private Button okButton,cancelButton;
            
    public void setDialog(Dialog<Void> dialog) {
        this.dialog = dialog;
        // Get the OK and CANCEL buttons
        
    }

    @FXML
    public void initialize() {
        

      
        // Set the action for the OK button
        if (ADD != null) {
            ADD.setOnAction(event -> {
                System.out.println("Handle OK");
                handleOK();
                closeDialog();
            });
        }

        // Set the action for the CANCEL button
        if (CANCEL != null) {
            CANCEL.setOnAction(event -> {
                System.out.println("Handle Cancel");
                handleCANCEL();
            });
        }


        // Set the action for the CANCEL button
        if (cancelButton != null) {
            cancelButton.setOnAction(event -> handleCANCEL());
        }

        // Initialize actions for GenerateRandomPassword button
        GenerateRandomPassword.setOnAction(event -> {
            PasswordData generatedPassword = PasswordGenerator.generatePassword(passwordLength);
            if (generatedPassword != null) {
                String decryptedPassword = decryptPassword(generatedPassword);
                passwordField.setText(decryptedPassword);
            } else {
                showAlert("Error", "Failed to generate password.");
            }
        });
        
    
     
    }
    
    private void handleOK() {
        // Close the dialog without saving
        System.out.println("handel ok");
        if (validateInput()) {
                // Perform actions to add the new password
                // Close the dialog
        }
    }
    
    private void handleCANCEL() {
        // Close the dialog without saving
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

        // Additional checks can be added here

        return true;
    }

    
    
    private void closeDialog() {
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.close();
    }

    private String decryptPassword(PasswordData passwordData) {
        try {
            // Decode the secret key from base64
            byte[] decodedKey = Base64.getDecoder().decode(passwordData.getSalt());
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            // Decode the encrypted password from base64
            byte[] encryptedBytes = Base64.getDecoder().decode(passwordData.getEncryptedPassword());

            // Decrypt the password using AES
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

        // Applying CSS to change the font
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        alert.showAndWait();
    }

    // Optional method to set password length
    public void setPasswordLength(int length) {
        this.passwordLength = length;
    }
}
