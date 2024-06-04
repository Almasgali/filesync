package ru.almasgali.filesync_server.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.almasgali.filesync_server.data.dto.FileResponse;
import ru.almasgali.filesync_server.data.model.File;
import ru.almasgali.filesync_server.exceptions.storage.StorageException;
import ru.almasgali.filesync_server.exceptions.storage.StorageFileNotFoundException;
import ru.almasgali.filesync_server.repository.FileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class StorageService {

    private final Path rootLocation = Path.of("./storage");
    private final FileRepository fileRepository;

    public StorageService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        init();
    }

    public void store(MultipartFile file, long updatedAt, String username) {
        String filename = file.getOriginalFilename();
        Optional<File> existing = fileRepository.findByNameAndUsername(filename, username);
        if (existing.isPresent()) {
            if (existing.get()
                    .getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli() > updatedAt) {
                return;
            }
        }
        File fileInfo = File.builder()
                .name(filename)
                .username(username)
                .createdAt(LocalDateTime.now()).build();
        fileRepository.save(fileInfo);
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation
                    .resolve(Path.of(username))
                    .resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            Files.createDirectories(destinationFile);
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
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

    public Resource loadAsResource(String filename, String username) {
        try {
            Path file = load(filename, username);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
