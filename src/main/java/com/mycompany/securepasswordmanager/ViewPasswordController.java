package com.mycompany.securepasswordmanager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Dialog;

public class ViewPasswordController {

    @FXML
    private Button addPasswordButton;

    @FXML
    public void initialize() {
        addPasswordButton.setOnAction(event -> openAddPasswordDialog());
    }

    private void openAddPasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddNewPasswordDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Get the controller
            AddNewPasswordDialogController controller = loader.getController();

            // Set the font (if needed)
            dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addPasswordButton.getScene().getWindow());
            
            // Set the width and height
            dialogStage.setWidth(500); // Set your desired width
            dialogStage.setHeight(500); // Set your desired height
            
            // Make the dialog non-resizable
            dialogStage.setResizable(false);
            
            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);

            // Set the dialog in the controller (if needed)
            controller.setDialog(new Dialog<>());
            
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
