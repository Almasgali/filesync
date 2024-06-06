package ru.almasgali.filesync_server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.almasgali.filesync_server.service.StorageService;

import java.util.List;

@RestController
@RequestMapping("/files")
@Validated
public class FileController {

    @Autowired
    private StorageService storageService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<String> getFiles(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return storageService.loadAll(userDetails.getUsername());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam("updated_at")
                           long updatedAt,
                           Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        storageService.store(file, updatedAt, userDetails.getUsername());
    }

    @ResponseBody
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(
            @PathVariable
            @Valid
            String filename,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Resource file = storageService.loadAsResource(userDetails.getUsername(), filename);

        if (file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
