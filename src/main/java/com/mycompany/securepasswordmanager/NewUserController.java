/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 *
 * @author mac
 */
    
    
    public class NewUserController {
    
    @FXML
    private TextField UserNameTextField;
    
    @FXML
    private PasswordField PasswordTextField;
    
    @FXML
    private Label CreateUserMessage;
    
    public void LoginButtonOn(ActionEvent e)
    {
      
        if (UserNameTextField.getText().isBlank()==false && PasswordTextField.getText().isBlank()==false)
        {
            CreateUserMessage.setText("You try to create user");
        }
        else
        {
            CreateUserMessage.setText("Please Enter username and password !!");
        }
    }
    
    //function to change between login screen and create 
    //user screen in using goTologin button 
    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
