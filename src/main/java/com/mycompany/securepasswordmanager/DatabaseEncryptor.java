package com.mycompany.securepasswordmanager;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

public class DatabaseEncryptor {
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private final SecretKey secretKey;

    public DatabaseEncryptor(String keyFilePath) throws Exception {
        Path keyPath = Paths.get(keyFilePath);
        if (Files.exists(keyPath)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyPath.toFile()))) {
                byte[] keyBytes = (byte[]) ois.readObject();
                secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            }
        } else {
            // Create directories if they do not exist
            if (!Files.exists(keyPath.getParent())) {
                Files.createDirectories(keyPath.getParent());
            }
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE, new SecureRandom());
            secretKey = keyGen.generateKey();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(keyPath.toFile()))) {
                oos.writeObject(secretKey.getEncoded());
            }
        }
    }

    public void encrypt(String inputFilePath, String outputFilePath) throws Exception {
        processFile(Cipher.ENCRYPT_MODE, inputFilePath, outputFilePath);
    }

    public void decrypt(String inputFilePath, String outputFilePath) throws Exception {
        processFile(Cipher.DECRYPT_MODE, inputFilePath, outputFilePath);
    }

    private void processFile(int cipherMode, String inputFilePath, String outputFilePath) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(cipherMode, secretKey);
        Path inputPath = Paths.get(inputFilePath);
        Path outputPath = Paths.get(outputFilePath);

        try (FileInputStream fis = new FileInputStream(inputPath.toFile());
             FileOutputStream fos = new FileOutputStream(outputPath.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] bytes = new byte[1024];
            int numBytes;
            while ((numBytes = fis.read(bytes)) != -1) {
                cos.write(bytes, 0, numBytes);
            }
        }
    }
}
