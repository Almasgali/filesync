package ru.almasgali.filesync_server.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthRequest {
    private String username;
    private String password;
}
