package com.mycompany.securepasswordmanager;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class EditUserDetailsController {

    private Stage dialogStage;

    @FXML
    private TextField CurrentUserNameField;
            
    @FXML
    private TextField CurrentFirstNameField;
    
    @FXML
    private TextField CurrentLastNameField;
    
    @FXML
    private TextField CurrentEmailField;
    
    @FXML
    private TextField CurrentPhoneNumberField;
    
    @FXML
    private TextField CurrentPasswordField;
    
    @FXML
    private TextField UserNameField;
            
    @FXML
    private TextField FirstNameField;
    
    @FXML
    private TextField LastNameField;
    
    @FXML
    private TextField EmailField;
    
    @FXML
    private TextField PhoneNumberField;
    
    @FXML
    private PasswordField PasswordField;
    
    @FXML
    private PasswordField RetypePasswordField;
    

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Method to initialize the fields from the UserSession
    @FXML
    public void initialize() {
        UserSession userSession = UserSession.getInstance();
        
        try {
            
            // Set the fields in the UI
            CurrentUserNameField.setText(userSession.getUsername());
            CurrentFirstNameField.setText(userSession.getFirstName());
            CurrentLastNameField.setText(userSession.getLastName());
            CurrentEmailField.setText(userSession.getEmailId());
            CurrentPhoneNumberField.setText(userSession.getPhoneNumber());
            CurrentPasswordField.setText(userSession.getUserPassword());
        
        } catch (Exception e) {
            System.err.println("Error initializing user details: " + e.getMessage());
            // You might want to show an error dialog to the user here
        }
    }

    @FXML
    private void handleSave() throws Exception {
        // Handle saving the edited user details
        // You can add code here to update the UserSession and the database
        preprocessingPassword();
        
    }

    @FXML
    private void handleCancel() {
        // Handle cancel action
        dialogStage.close();
    }
    
    private void preprocessingPassword() throws Exception {
        String userName, password, firstName, lastName, email, phoneNo;

        userName = UserNameField.getText() != null && !UserNameField.getText().isEmpty() ? UserNameField.getText() : CurrentUserNameField.getText();
        firstName = FirstNameField.getText() != null && !FirstNameField.getText().isEmpty() ? FirstNameField.getText() : CurrentFirstNameField.getText();
        lastName = LastNameField.getText() != null && !LastNameField.getText().isEmpty() ? LastNameField.getText() : CurrentLastNameField.getText();
        email = EmailField.getText() != null && !EmailField.getText().isEmpty() ? EmailField.getText() : CurrentEmailField.getText();
        phoneNo = PhoneNumberField.getText() != null && !PhoneNumberField.getText().isEmpty() ? PhoneNumberField.getText() : CurrentPhoneNumberField.getText();

        // Check if a new password has been entered
        if (PasswordField.getText() != null && !PasswordField.getText().isEmpty()) {
            // Check if retype password matches the new password
            if (PasswordField.getText().equals(RetypePasswordField.getText())) {
                // Passwords match, proceed with new password
                password = PasswordField.getText();
            } else {
                // Passwords do not match, show an error message and return
                showAlert("Password Mismatch", "New Password and Retype Password do not match.");
                return; // Stop further execution and allow the user to correct the input
            }
        } else {
            // If no new password is provided, keep the current password
            password = CurrentPasswordField.getText();
        }

        if (UserNameField.getText()!=null && !UserNameField.getText().isEmpty()){
          //check if same username exist
          if(userExists(UserNameField.getText())){
              showAlert("Username Exist", "This Username is taken ");
              return; 
          }
        }

        //now go to one function to encript and update passwordmanager.db
        if (!UpdatePasswordManagerDatabase(userName,password)){
            showAlert("Error in Updating", "Cannot update passwordmanage.db file ");
            return;
        
        }
        
        //now go and update userdatabase.db
        UserSession userSession = UserSession.getInstance();

        if(!userSession.updateUserInformation(userName, firstName, lastName, email, phoneNo)){
            showAlert("Error in Updating", "Cannot update userdatabase file ");
            return;
        }
        else{
             UpdateSessionData(userName, password, firstName, lastName, email, phoneNo);   
             
             dialogStage.close();
        }
        

    }

    private boolean userExists(String username) throws SQLException, Exception {
            return Database.userExists(username);
        }    

    private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setMinHeight(Region.USE_PREF_SIZE);
            dialogPane.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

            alert.showAndWait();
        }

    private boolean UpdatePasswordManagerDatabase(String UserName, String Password) throws NoSuchAlgorithmException, Exception{
        
        UserSession userSession = UserSession.getInstance();

//        // Hash the password with a salt
//        System.out.println("username: "+UserName);
//        System.out.println("Password: "+Password);

        
        String salt = SecurityUtils.generateSalt();
        String hashedPassword = SecurityUtils.hashData(Password, salt);
        String encryptUsername = Database.encryptUsername(UserName); // Common database using database secret key
        Database.updateUserDetails(userSession.getUserID(),encryptUsername, hashedPassword, salt);
        return true;

    }
    
    private boolean UpdateSessionData(String userName, String password, String firstName, String lastName, String email, String phoneNo){ 
        UserSession userSession = UserSession.getInstance();
        userSession.setUsername(userName);
        userSession.setUserPassword(password);
        userSession.setFirstName(firstName);
        userSession.setLastName(lastName);
        userSession.setEmailId(email);
        userSession.setPhoneNumber(phoneNo);
        return true;
    }

 
   
  
}
