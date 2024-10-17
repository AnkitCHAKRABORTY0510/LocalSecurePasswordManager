package com.mycompany.securepasswordmanager;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.nio.file.Paths; // Import Paths
import javafx.scene.control.Tooltip;

public class ViewPasswordController {

    @FXML
    private VBox passwordsContainer;

    @FXML
    private Button addPasswordButton;

    @FXML
    public void initialize() throws Exception {
        try {
            addPasswordButton.setOnAction(event -> {
                try {
                    openAddPasswordDialog();
                } catch (Exception ex) {
                    Logger.getLogger(ViewPasswordController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            loadPasswordsFromDatabase();
        } catch (Exception ex) {
            Logger.getLogger(ViewPasswordController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void openAddPasswordDialog() throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Fxml/AddNewPasswordDialog.fxml"));
            DialogPane dialogPane = loader.load();

            AddNewPasswordDialogController controller = loader.getController();
            dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Password");
            
            
            
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addPasswordButton.getScene().getWindow());

            dialogStage.setWidth(500);
            dialogStage.setHeight(500);
            dialogStage.setResizable(false);

            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);

            controller.setDialog(new Dialog<>());

            dialogStage.showAndWait();

            loadPasswordsFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadPasswordsFromDatabase() throws Exception {
    UserSession userSession = UserSession.getInstance();
    String dbUrl = "jdbc:sqlite:" + Paths.get("data", "users", userSession.getUserID() + ".db").toString(); // Platform-independent path
    String selectSQL = "SELECT id, url, Account, encrypted_password, salt, description FROM passwords";
    UserSession session = UserSession.getInstance();
    try (Connection connection = DriverManager.getConnection(dbUrl);
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(selectSQL)) {

        List<PasswordData> passwordList = new ArrayList<>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
          
            String url = resultSet.getString("url");
            url=EncryptionUtils.decrypt(url,session.getUserSecretKey(),session.getUserIv());
            
            String Account = resultSet.getString("Account");
            Account=EncryptionUtils.decrypt(Account,session.getUserSecretKey(),session.getUserIv());
            
            String encryptedPassword = resultSet.getString("encrypted_password");
            encryptedPassword=EncryptionUtils.decrypt(encryptedPassword,session.getUserSecretKey(),session.getUserIv());
            
            String salt = resultSet.getString("salt");
            salt=EncryptionUtils.decrypt(salt,session.getUserSecretKey(),session.getUserIv());
            
            
            String description = resultSet.getString("description");
            description=EncryptionUtils.decrypt(description,session.getUserSecretKey(),session.getUserIv());
            
            String decryptPassword=decryptPassword(encryptedPassword,salt);//to get mine original password
            passwordList.add(new PasswordData(id, url ,Account, encryptedPassword, decryptPassword, salt, description));
            
            
        }

        displayPasswords(passwordList);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    private void loadImage(ImageView imageView, String imagePath) {
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            System.err.println("Error: Image not found at " + imagePath);
        } else {
            imageView.setImage(new Image(imageStream));
        }
    }
    
    private String decryptPassword(PasswordData passwordData) {
        try {
            System.out.println("View Password \n "+ passwordData.getUrl());
            byte[] decodedSalt = Base64.getDecoder().decode(passwordData.getSalt());
            byte[] decodedKey = generateKey(decodedSalt);
            System.out.println("decoded salt "+ decodedKey );
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            byte[] encryptedBytes = Base64.getDecoder().decode(passwordData.getEncryptedPassword());
            System.out.println("encryptedPassword"+ encryptedBytes);
            
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
    
    private String decryptPassword(String EncryptedPassword,String salt) {
        try {
            
            byte[] decodedSalt = Base64.getDecoder().decode(salt);
            byte[] decodedKey = generateKey(decodedSalt);
           
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            byte[] encryptedBytes = Base64.getDecoder().decode(EncryptedPassword);
            
            
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

    private byte[] generateKey(byte[] salt) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(salt);
            keyGen.init(128, secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate key.");
            return null;
        }
    }

    
    

    private void displayPasswords(List<PasswordData> passwordList) {
        passwordsContainer.getChildren().clear();

        for (PasswordData passwordData : passwordList) {
            String decryptedPassword = decryptPassword(passwordData);

            HBox hBox = new HBox();
            hBox.setPrefHeight(35.0);
            hBox.setPrefWidth(995.0);
            hBox.setSpacing(10);

            Label urlLabel = new Label(passwordData.getUrl());
            urlLabel.setPrefHeight(35.0);
            urlLabel.setPrefWidth(300.0);
            urlLabel.setTextAlignment(TextAlignment.CENTER);
            urlLabel.setAlignment(Pos.CENTER);
            urlLabel.setFont(new Font("Avenir Black", 14.0));
            urlLabel.setStyle("-fx-border-color: blue; -fx-border-radius: 5; -fx-background-color: #E3F4FB;");
            HBox.setHgrow(urlLabel, Priority.ALWAYS);
            //tooltip 
            // Create the Tooltip
            Tooltip urltooltip = new Tooltip();
            urltooltip.setText(passwordData.getUrl()); // Bind the tooltip text to the label text
            urltooltip.setTextAlignment(TextAlignment.CENTER);
            urltooltip.setWrapText(true);
            urltooltip.setFont(new Font("Avenir Black", 12.0));
            
            urltooltip.setShowDelay(Duration.seconds(0.1));
            urltooltip.setHideDelay(Duration.seconds(0.1));
            // Assign the Tooltip to the Label
            urlLabel.setTooltip(urltooltip);

            Label accountLabel = new Label(passwordData.getAccount());
            accountLabel.setPrefHeight(35.0);
            accountLabel.setPrefWidth(235.0);
            accountLabel.setTextAlignment(TextAlignment.CENTER);
            accountLabel.setAlignment(Pos.CENTER);
            accountLabel.setFont(new Font("Avenir Black", 14.0));
            accountLabel.setStyle("-fx-border-color: blue; -fx-border-radius: 5; -fx-background-color: #E3F4FB;");
            SepiaTone sepiaTone = new SepiaTone();
            sepiaTone.setLevel(0.42);
            accountLabel.setEffect(sepiaTone);
            HBox.setHgrow(accountLabel, Priority.ALWAYS);
            
            //tooltip 
            // Create the Tooltip
            Tooltip Accounttooltip = new Tooltip();
            Accounttooltip.setText(passwordData.getAccount()); // Bind the tooltip text to the label text
            Accounttooltip.setTextAlignment(TextAlignment.CENTER);
            Accounttooltip.setWrapText(true);
            Accounttooltip.setFont(new Font("Avenir Black", 12.0));
            
            Accounttooltip.setShowDelay(Duration.seconds(0.1));
            Accounttooltip.setHideDelay(Duration.seconds(0.1));
            // Assign the Tooltip to the Label
            accountLabel.setTooltip(Accounttooltip);

            TextField passwordTextField = new TextField();
            passwordTextField.setText(decryptedPassword);
            passwordTextField.setPrefHeight(35.0);
            passwordTextField.setPrefWidth(225.0);
            passwordTextField.setFont(new Font("Avenir Black", 14.0));
            passwordTextField.setEditable(false);
            passwordTextField.setVisible(false);

            PasswordField passwordField = new PasswordField();
            passwordField.setText(decryptedPassword);
            passwordField.setPrefHeight(35.0);
            passwordField.setPrefWidth(225.0);
            passwordField.setEditable(false);

            passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
            passwordField.managedProperty().bind(passwordField.visibleProperty());
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

            AnchorPane passwordFieldContainer = new AnchorPane();
            passwordFieldContainer.setPrefHeight(35.0);
            passwordFieldContainer.setPrefWidth(225.0);
            passwordFieldContainer.getChildren().addAll(passwordTextField, passwordField);
            AnchorPane.setRightAnchor(passwordField, 0.0);

            ImageView toggleVisibility = new ImageView();
            toggleVisibility.setFitHeight(35.0);
            toggleVisibility.setFitWidth(35.0);
            loadImage(toggleVisibility, "Images/passwordshow.png");
            toggleVisibility.setCursor(Cursor.HAND);

            toggleVisibility.setOnMouseClicked(event -> {
                if (passwordTextField.isVisible()) {
                    passwordTextField.setVisible(false);
                    passwordField.setVisible(true);
                    loadImage(toggleVisibility, "Images/passwordshow.png");
                } else {
                    passwordTextField.setVisible(true);
                    passwordField.setVisible(false);
                    loadImage(toggleVisibility, "Images/passwordhide.png");
                }
            });

            AnchorPane visibilityContainer = new AnchorPane();
            visibilityContainer.setPrefHeight(35.0);
            visibilityContainer.setPrefWidth(35.0);
            visibilityContainer.getChildren().add(toggleVisibility);
            AnchorPane.setBottomAnchor(toggleVisibility, 5.0);
            AnchorPane.setLeftAnchor(toggleVisibility, 5.0);
            AnchorPane.setRightAnchor(toggleVisibility, 5.0);
            AnchorPane.setTopAnchor(toggleVisibility, 5.0);

            ImageView infoImage = new ImageView();
            infoImage.setFitHeight(35.0);
            infoImage.setFitWidth(35.0);
            loadImage(infoImage, "Images/info.png");
            infoImage.setCursor(Cursor.OPEN_HAND);

            infoImage.setOnMouseClicked(event -> {
                showInfoDialog(passwordData);
            });

            AnchorPane infoContainer = new AnchorPane();
            infoContainer.setPrefHeight(35.0);
            infoContainer.setPrefWidth(35.0);
            infoContainer.getChildren().add(infoImage);
            AnchorPane.setBottomAnchor(infoImage, 5.0);
            AnchorPane.setLeftAnchor(infoImage, 5.0);
            AnchorPane.setRightAnchor(infoImage, 5.0);
            AnchorPane.setTopAnchor(infoImage, 5.0);

            ImageView editImage = new ImageView();
            editImage.setFitHeight(35.0);
            editImage.setFitWidth(35.0);
            loadImage(editImage, "Images/edit.png");
            editImage.setCursor(Cursor.HAND);

            editImage.setOnMouseClicked(event -> {
                try {
                    openEditDialog(passwordData);
                } catch (Exception ex) {
                    Logger.getLogger(ViewPasswordController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            AnchorPane editContainer = new AnchorPane();
            editContainer.setPrefHeight(35.0);
            editContainer.setPrefWidth(35.0);
            editContainer.getChildren().add(editImage);
            AnchorPane.setBottomAnchor(editImage, 5.0);
            AnchorPane.setLeftAnchor(editImage, 5.0);
            AnchorPane.setRightAnchor(editImage, 5.0);
            AnchorPane.setTopAnchor(editImage, 5.0);

            ImageView copyImage = new ImageView();
            copyImage.setFitHeight(35.0);
            copyImage.setFitWidth(35.0);
            loadImage(copyImage, "Images/copy.png");
            copyImage.setCursor(Cursor.HAND);

            copyImage.setOnMouseClicked(event -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(decryptedPassword);
                clipboard.setContent(content);
                showConfirmation("COPY TO CLIPBOARD", "Password: " + decryptedPassword, "Password copied to clipboard!");
            });

            AnchorPane copyContainer = new AnchorPane();
            copyContainer.setPrefHeight(35.0);
            copyContainer.setPrefWidth(35.0);
            copyContainer.getChildren().add(copyImage);
            AnchorPane.setBottomAnchor(copyImage, 5.0);
            AnchorPane.setLeftAnchor(copyImage, 5.0);
            AnchorPane.setRightAnchor(copyImage, 5.0);
            AnchorPane.setTopAnchor(copyImage, 5.0);

            hBox.getChildren().addAll(urlLabel, accountLabel, passwordFieldContainer, visibilityContainer, infoContainer, editContainer, copyContainer);
            passwordsContainer.getChildren().add(hBox);
        }
    }
    

    
    private void showInfoDialog(PasswordData passwordData) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PASSWORD ID: " + passwordData.getId());
        alert.setHeaderText("Details for: " + passwordData.getUrl());
        alert.setContentText("Account: "+ passwordData.getAccount() + "\nDescription: " + passwordData.getDescription());

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        // Set the owner of the alert to the main application window
        Stage ownerStage = (Stage) passwordsContainer.getScene().getWindow(); // Replace 'someNode' with a node in your current scene
        alert.initOwner(ownerStage);

        // Set modality to ensure the alert is modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Optionally, set the preferred size
        dialogPane.setPrefSize(400, 300); // Adjust as needed

        alert.showAndWait();

   }
    
   private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

       DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        // Set the owner of the alert to the main application window
        Stage ownerStage = (Stage) passwordsContainer.getScene().getWindow(); // Replace 'someNode' with a node in your current scene
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
   private void showConfirmation(String title, String message, String text) {


    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(text);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        // Set the owner of the alert to the main application window
        Stage ownerStage = (Stage) passwordsContainer.getScene().getWindow(); // Replace 'someNode' with a node in your current scene
        alert.initOwner(ownerStage);

        // Set modality to ensure the alert is modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Optionally, set the preferred size
        dialogPane.setPrefSize(300, 200); // Adjust as needed

       

        alert.showAndWait();
    }
   
   private void openEditDialog(PasswordData passwordData) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Fxml/EditPasswordDialog.fxml"));
            DialogPane dialogPane = loader.load();

            EditPasswordDialogController controller = loader.getController();
            controller.setPasswordData(passwordData);

            dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addPasswordButton.getScene().getWindow());

            dialogStage.setWidth(500);
            dialogStage.setHeight(500);
            dialogStage.setResizable(false);

            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);

            controller.setDialog(new Dialog<>());

            dialogStage.showAndWait();

            loadPasswordsFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}