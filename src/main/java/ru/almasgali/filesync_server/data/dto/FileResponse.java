package ru.almasgali.filesync_server.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileResponse {
    private String name;
    private String hash;
}
