package ru.almasgali.filesync_server.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.almasgali.filesync_server.data.dto.FileResponse;
import ru.almasgali.filesync_server.exceptions.storage.StorageException;
import ru.almasgali.filesync_server.exceptions.storage.StorageFileNotFoundException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class StorageService {

    private final Path rootLocation = Path.of("/storage");
    private final Cipher cipher;
    private SecretKey secretKey;

    public StorageService() {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new StorageException("Could not initialize storage", e);
        }
        init();
    }

    public void store(MultipartFile file, String username) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation
                    .resolve(Path.of(username))
                    .resolve(Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            Files.createDirectories(destinationFile);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            try (CipherInputStream cis = new CipherInputStream(file.getInputStream(), cipher)) {
                Files.copy(cis, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        } catch (InvalidKeyException e) {
            throw new StorageException("Invalid secret key.", e);
        }
    }

    public List<FileResponse> loadAll(String username) {
        Path fullPath = this.rootLocation.resolve(username);
        try {
            return Files.walk(fullPath, 1)
                    .filter(path -> !path.equals(fullPath))
                    .map(path -> {
                        try {
                            return FileResponse.builder()
                                    .name(path.getFileName().toString())
                                    .hash(DigestUtils.md5DigestAsHex(Files.newInputStream(path))).build();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    public Path load(String filename, String username) {
        return rootLocation.resolve(username).resolve(filename);
    }

    public void writeFileToResponse(String filename, String username, HttpServletResponse response) {
        try {
            Path file = load(filename, username);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "filename=\""+ filename + "\"");
            try (CipherInputStream cis = new CipherInputStream(Files.newInputStream(file), cipher);
                 OutputStream os = response.getOutputStream()) {
                IOUtils.copy(cis, os);
            }
        } catch (IOException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        } catch (InvalidKeyException e) {
            throw new StorageException("Invalid secret key.", e);
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Path path = Path.of("/storage/secret.key");
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                secretKey = (SecretKey) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new StorageException("Could not initialize storage", e);
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
