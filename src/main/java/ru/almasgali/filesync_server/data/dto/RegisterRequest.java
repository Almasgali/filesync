package ru.almasgali.filesync_server.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegisterRequest {
    private String username;
    private String password;
}
