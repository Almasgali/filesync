package ru.almasgali.filesync_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.almasgali.filesync_server.data.dto.AuthRequest;
import ru.almasgali.filesync_server.data.dto.AuthResponse;
import ru.almasgali.filesync_server.data.dto.RegisterRequest;
import ru.almasgali.filesync_server.data.model.User;
import ru.almasgali.filesync_server.service.AuthService;
import ru.almasgali.filesync_server.service.JwtService;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        AuthResponse loginResponse = AuthResponse.builder().token(jwtToken).build();

        return ResponseEntity.ok(loginResponse);
    }
}
