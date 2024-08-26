package com.mycompany.securepasswordmanager;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {

    @FXML
    private Pane contentPane;

    @FXML
    private Label UserName;
    
    @FXML 
    private Button addnewpassword;
    
    private String username;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        username = session.getUsername();

        UserName.setText("WELCOME ! " + username);
        loadViewPassword();
    }
    
    @FXML
    private void handleAddNewPasswordClick() {
        try {
            openAddPasswordDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        // Perform logout
        UserSession.getInstance().logout();

        try {
            showAlert("Logout Message", username + " - Logout Successful!");
            App.setRoot("login", 900, 550);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUserDetails() {
        loadFXMLIntoPane("UserDetails");
    }

    public void loadViewPassword() {
        loadFXMLIntoPane("ViewPassword");
    }

    public void loadView3() {
        loadFXMLIntoPane("View3");
    }

    private void loadFXMLIntoPane(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Fxml/" + fxml + ".fxml"));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //--------------------------------------------------------------
        UserSession session = UserSession.getInstance();
        username = session.getUsername();

        UserName.setText("WELCOME ! " + username);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        // Set the owner of the alert to the main application window
        Stage ownerStage = (Stage) UserName.getScene().getWindow();
        alert.initOwner(ownerStage);

        // Set modality to ensure the alert is modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Optionally, set the preferred size
        dialogPane.setPrefSize(300, 120); // Adjust as needed

        // Create a Timeline that will close the alert after 5 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> alert.close()));
        timeline.setCycleCount(1);
        timeline.play();

        alert.show();
    }

    private void openAddPasswordDialog() throws Exception {
        try {
            
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Fxml/AddNewPasswordDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Print the class type of the returned object
            System.out.println("Loaded object class: " + dialogPane.getClass().getName());

            // Additional check to see if the dialogPane is not null
            if (dialogPane != null) {
                System.out.println("DialogPane loaded successfully.");
            } else {
                System.out.println("Failed to load DialogPane.");
}


            AddNewPasswordDialogController controller = loader.getController();
            dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addnewpassword.getScene().getWindow());

            dialogStage.setWidth(500);
            dialogStage.setHeight(500);
            dialogStage.setResizable(false);

            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);

            controller.setDialog(new Dialog<>());

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 

}
