package ru.almasgali.filesync_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.almasgali.filesync_server.data.dto.FileResponse;
import ru.almasgali.filesync_server.data.model.File;
import ru.almasgali.filesync_server.exceptions.common.ConstraintViolationException;
import ru.almasgali.filesync_server.exceptions.storage.OldVersionException;
import ru.almasgali.filesync_server.exceptions.storage.StorageException;
import ru.almasgali.filesync_server.exceptions.storage.FileNotFoundException;
import ru.almasgali.filesync_server.repository.FileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class StorageService {


    private final Path rootLocation;

    @Autowired
    private FileRepository fileRepository;

    public StorageService(@Value("${storage.root-location}") String root) {
        this.rootLocation = Path.of(root);
        init();
    }

    public void store(MultipartFile file, long updatedAt, String username) {

        if (updatedAt < 0) {
            throw new ConstraintViolationException("updatedAt should be positive");
        }

        String filename = file.getOriginalFilename();
        Optional<File> existing = fileRepository.findByNameAndUsername(filename, username);
        if (existing.isPresent()) {
            long exisitingUpdatedAt = existing.get()
                    .getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            if (exisitingUpdatedAt > updatedAt) {
                throw new OldVersionException("Existing file has newer version");
            } else {
                fileRepository.deleteById(existing.get().getId());
            }
        }
        File fileInfo = File.builder()
                .name(filename)
                .username(username)
                .createdAt(LocalDateTime.now()).build();
        fileRepository.save(fileInfo);
        try {
            Path destinationFile = load(username, filename)
                    .normalize().toAbsolutePath();

            Files.createDirectories(destinationFile.getParent());
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    public List<FileResponse> loadAll(String username) {
        return fileRepository.findByUsername(username).stream().map(
                f -> FileResponse.builder()
                        .name(f.getName())
                        .updatedAt(f.getUpdatedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant().toEpochMilli()).build()).toList();
//        Path fullPath = this.rootLocation.resolve(hashUsername(username));
//        try {
//            return Files.walk(fullPath, 1)
//                    .filter(path -> !path.equals(fullPath))
//                    .map(path -> FileResponse.builder()
//                            .name(path.getFileName().toString())
//                            .updatedAt()).toList();
//        } catch (IOException e) {
//            throw new StorageException("Failed to read stored files", e);
//        }

    }

    public Path load(String username, String filename) {
        return rootLocation
                .resolve(hashUsername(username))
                .resolve(filename);
    }

    public Resource loadAsResource(String username, String filename) {

        if (filename.isBlank()) {
            throw new ConstraintViolationException("Filename shouldn't be blank");
        }

        try {
            Path file = load(username, filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException(
                        "File not found: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    private String hashUsername(String username) {
        return DigestUtils.md5DigestAsHex(username.getBytes(StandardCharsets.UTF_8));
    }
}
