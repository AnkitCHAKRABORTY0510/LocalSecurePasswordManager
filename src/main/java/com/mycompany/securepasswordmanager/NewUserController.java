/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.securepasswordmanager;

import java.io.IOException;
import javafx.fxml.FXML;

/**
 *
 * @author mac
 */
public class NewUserController {
     @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
