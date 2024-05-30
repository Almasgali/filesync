package ru.almasgali.filesync_server.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.almasgali.filesync_server.data.dto.FileRequest;
import ru.almasgali.filesync_server.data.dto.FileResponse;
import ru.almasgali.filesync_server.service.StorageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final StorageService storageService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<FileResponse> getFiles(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return storageService.loadAll(userDetails.getUsername());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadFile(@RequestParam("file") MultipartFile file,
                           Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        storageService.store(file, userDetails.getUsername());
    }

    @ResponseBody
    @GetMapping("/{filename}")
    public void getFile(@PathVariable String filename,
                        HttpServletResponse response,
                        Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        storageService.writeFileToResponse(filename, userDetails.getUsername(), response);
    }
}
