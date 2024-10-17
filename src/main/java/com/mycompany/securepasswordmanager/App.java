package com.mycompany.securepasswordmanager;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends Application {

    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data";
    private static final String DATABASE_FILE = DATA_DIR + File.separator + "password_manager.db";
    private static final String ENCRYPTED_DATABASE_FILE = DATA_DIR + File.separator + "encrypted_password_manager.db";
    private static final String KEY_FILE = DATA_DIR + File.separator + "keyfile.key";
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
        scene = new Scene(loadFXML("login"), 900, 550);

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
    // Use the correct path to the FXML files
    URL loader = App.class.getResource("Fxml/" + fxml + ".fxml");
    
    if (loader != null) {
        System.out.println("Resource found: " + loader.toString());
    } else {
        System.out.println("Resource not found.");
    }
    
    FXMLLoader fxmlLoader = new FXMLLoader(loader);
    return fxmlLoader.load();
}


    public static void main(String[] args) {
        launch();
    }
}
