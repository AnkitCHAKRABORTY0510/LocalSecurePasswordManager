/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

/**
 *
 * @author mac
 */


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {
    @FXML
    private Pane contentPane;

    public void loadUserDetails() {
        loadFXMLIntoPane("UserDetails.fxml");
    }

    public void loadView2() {
        loadFXMLIntoPane("View2.fxml");
    }

    public void loadView3() {
        loadFXMLIntoPane("View3.fxml");
    }

    private void loadFXMLIntoPane(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}