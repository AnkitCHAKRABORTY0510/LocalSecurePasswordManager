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

public class ViewPasswordController {

    @FXML
    private VBox passwordsContainer;

    @FXML
    private Button addPasswordButton;

    @FXML
    public void initialize() {
        addPasswordButton.setOnAction(event -> openAddPasswordDialog());
        loadPasswordsFromDatabase();
    }

    private void openAddPasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddNewPasswordDialog.fxml"));
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

    private void loadPasswordsFromDatabase() {
    UserSession userSession = UserSession.getInstance();
    String dbUrl = "jdbc:sqlite:data/users/" + userSession.getUserID() + ".db";
    String selectSQL = "SELECT id, url, encrypted_password, salt, description FROM passwords";

    try (Connection connection = DriverManager.getConnection(dbUrl);
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(selectSQL)) {

        List<PasswordData> passwordList = new ArrayList<>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String url = resultSet.getString("url");
            String encryptedPassword = resultSet.getString("encrypted_password");
            String salt = resultSet.getString("salt");
            String description = resultSet.getString("description");

            passwordList.add(new PasswordData(id, url, encryptedPassword, salt, description));
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
            byte[] decodedSalt = Base64.getDecoder().decode(passwordData.getSalt());
            byte[] decodedKey = generateKey(decodedSalt);
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
        hBox.setPrefHeight(413.0);
        hBox.setPrefWidth(725.0);
        hBox.setMaxHeight(Double.MAX_VALUE);
        hBox.setMaxWidth(Double.MAX_VALUE);
        hBox.setSpacing(10);

        Label urlLabel = new Label(passwordData.getUrl());
        urlLabel.setPrefHeight(35.0);
        urlLabel.setPrefWidth(399.0);
        urlLabel.setTextAlignment(TextAlignment.CENTER);
        urlLabel.setAlignment(Pos.CENTER);
        urlLabel.setFont(new Font("Avenir Black", 14.0));
        SepiaTone sepiaTone = new SepiaTone();
        sepiaTone.setLevel(0.42);
        urlLabel.setEffect(sepiaTone);
        HBox.setHgrow(urlLabel, Priority.ALWAYS);

        TextField passwordTextField = new TextField();
        passwordTextField.setText(decryptedPassword);
        passwordTextField.setPrefHeight(35.0);
        passwordTextField.setPrefWidth(345.0);
        passwordTextField.setFont(new Font("Avenir Black", 14.0));
        passwordTextField.setEditable(false);
        passwordTextField.setVisible(false);

        PasswordField passwordField = new PasswordField();
        passwordField.setText(decryptedPassword);
        passwordField.setPrefHeight(35.0);
        passwordField.setPrefWidth(349.0);
        passwordField.setEditable(false);

        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordField.managedProperty().bind(passwordField.visibleProperty());
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        AnchorPane passwordFieldContainer = new AnchorPane();
        passwordFieldContainer.setPrefHeight(465.0);
        passwordFieldContainer.setPrefWidth(381.0);
        passwordFieldContainer.setMaxHeight(Double.MAX_VALUE);
        passwordFieldContainer.setMaxWidth(Double.MAX_VALUE);

        passwordFieldContainer.getChildren().addAll(passwordTextField, passwordField);
        AnchorPane.setRightAnchor(passwordField, 5.0);

        ImageView toggleVisibility = new ImageView();
        toggleVisibility.setFitHeight(35.0);
        toggleVisibility.setFitWidth(38.0);
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

        ImageView infoImage = new ImageView();
        infoImage.setFitHeight(36.0);
        infoImage.setFitWidth(37.0);
        loadImage(infoImage, "Images/info.png");
        infoImage.setCursor(Cursor.OPEN_HAND);

        infoImage.setOnMouseClicked(event -> {
            showInfoDialog(passwordData);
        });

        ImageView editImage = new ImageView();
        editImage.setFitHeight(35.0);
        editImage.setFitWidth(36.0);
        loadImage(editImage, "Images/edit.png");
        editImage.setCursor(Cursor.HAND);
        
        // Handle the edit functionality
        editImage.setOnMouseClicked(event -> {
            openEditDialog(passwordData);
        });
        
        // Copy to clipboard ImageView
        ImageView copyImage = new ImageView();
        copyImage.setFitHeight(35.0);
        copyImage.setFitWidth(36.0);
        loadImage(copyImage, "Images/copy.png");
        copyImage.setCursor(Cursor.HAND);

        // Set the action for copying to clipboard
        copyImage.setOnMouseClicked(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(decryptedPassword);
            clipboard.setContent(content);
            showConfirmation("COPY TO CLIPBOARD","Password:   " +decryptedPassword + " ", "Password copied to clipboard!");
        });
        
        AnchorPane visibilityContainer = new AnchorPane();
        visibilityContainer.setPrefHeight(80.0);
        visibilityContainer.setPrefWidth(59.0);
        visibilityContainer.setMaxWidth(Double.MAX_VALUE);
        visibilityContainer.getChildren().add(toggleVisibility);

        AnchorPane.setBottomAnchor(toggleVisibility, 5.0);
        AnchorPane.setLeftAnchor(toggleVisibility, 5.0);
        AnchorPane.setRightAnchor(toggleVisibility, 5.0);
        AnchorPane.setTopAnchor(toggleVisibility, 5.0);

        AnchorPane infoContainer = new AnchorPane();
        infoContainer.setPrefHeight(465.0);
        infoContainer.setPrefWidth(100.0);
        infoContainer.getChildren().add(infoImage);

        AnchorPane.setBottomAnchor(infoImage, 5.0);
        AnchorPane.setLeftAnchor(infoImage, 5.0);
        AnchorPane.setRightAnchor(infoImage, 5.0);
        AnchorPane.setTopAnchor(infoImage, 5.0);

        AnchorPane editContainer = new AnchorPane();
        editContainer.setPrefHeight(465.0);
        editContainer.setPrefWidth(188.0);
        editContainer.getChildren().add(editImage);

        AnchorPane.setBottomAnchor(editImage, 5.0);
        AnchorPane.setLeftAnchor(editImage, 5.0);
        AnchorPane.setRightAnchor(editImage, 5.0);
        AnchorPane.setTopAnchor(editImage, 5.0);
        
        AnchorPane copyContainer = new AnchorPane();
        copyContainer.setPrefHeight(465.0);
        copyContainer.setPrefWidth(100.0);
        copyContainer.getChildren().add(copyImage);

        AnchorPane.setBottomAnchor(copyImage, 5.0);
        AnchorPane.setLeftAnchor(copyImage, 5.0);
        AnchorPane.setRightAnchor(copyImage, 5.0);
        AnchorPane.setTopAnchor(copyImage, 5.0);
        
       
        hBox.getChildren().addAll(urlLabel, passwordFieldContainer, visibilityContainer, infoContainer, editContainer, copyContainer);
        passwordsContainer.getChildren().add(hBox);
    }
}
    
    private void showInfoDialog(PasswordData passwordData) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PASSWORD ID: " + passwordData.getId());
        alert.setHeaderText("Details for: " + passwordData.getUrl());
        alert.setContentText("Description: " + passwordData.getDescription());

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
   
   private void openEditDialog(PasswordData passwordData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditPasswordDialog.fxml"));
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
