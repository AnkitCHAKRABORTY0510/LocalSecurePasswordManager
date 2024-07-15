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
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Pane contentPane;
    
    @FXML
    private Label UserName;
    
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        String username = session.getUsername();
        
        UserName.setText("Username : "+username);
        
        
        

        // You can now use the username and password as needed
    }
    
    public void loadUserDetails() {
        loadFXMLIntoPane("UserDetails.fxml");
    }

    public void loadViewPassword() {
        loadFXMLIntoPane("ViewPassword.fxml");
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