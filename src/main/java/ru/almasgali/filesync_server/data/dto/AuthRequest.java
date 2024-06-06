package ru.almasgali.filesync_server.data.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthRequest {
    @Size(min = 4, max = 50, message = "Username length should be in interval 4-50")
    private String username;
    @Size(min = 8, max = 128, message = "Password length should be in interval 8-128")
    private String password;
}
