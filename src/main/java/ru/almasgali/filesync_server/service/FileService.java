package ru.almasgali.filesync_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.almasgali.filesync_server.repository.FileRepository;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final StorageService storageService;


}
