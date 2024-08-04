package com.mycompany.securepasswordmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App extends Application {

    private static final String DATABASE_FILE = "data/password_manager.db";
    private static final String ENCRYPTED_DATABASE_FILE = "data/encrypted_Password_manager.db";
    private static final String KEY_FILE = "data/keyfile.key";
    private static Scene scene;
    private static DatabaseEncryptor databaseEncryptor;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            databaseEncryptor = new DatabaseEncryptor(KEY_FILE);
            if (Files.exists(Paths.get(ENCRYPTED_DATABASE_FILE))) {
                // If encrypted database exists, decrypt it
                databaseEncryptor.decrypt(ENCRYPTED_DATABASE_FILE, DATABASE_FILE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // Exit if there is an issue with encryption/decryption
        }

        // Ensure the database is encrypted when the application stops
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                databaseEncryptor.encrypt(DATABASE_FILE, ENCRYPTED_DATABASE_FILE);
                Files.deleteIfExists(Paths.get(DATABASE_FILE));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        // Initialize database if it doesn't exist
        if (!Files.exists(Paths.get(DATABASE_FILE))) {
            Database.createNewTables();
        }

        // Load the initial login scene
        scene = new Scene(loadFXML("login"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    
    static void setRoot(String fxml, double width, double height) throws IOException {
        Parent root = loadFXML(fxml);
        scene.setRoot(root);
        Stage stage = (Stage) scene.getWindow();
        stage.setWidth(width);
        stage.setHeight(height);
        // Apply the inline CSS for fonts
        scene.getRoot().setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
        
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
