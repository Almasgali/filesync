package ru.almasgali.filesync_server.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.almasgali.filesync_server.data.dto.AuthRequest;
import ru.almasgali.filesync_server.data.dto.AuthResponse;
import ru.almasgali.filesync_server.data.dto.RegisterRequest;
import ru.almasgali.filesync_server.data.model.User;
import ru.almasgali.filesync_server.service.AuthService;
import ru.almasgali.filesync_server.service.JwtService;

@RequestMapping("/user")
@RestController
public class AuthController {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid RegisterRequest registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        AuthResponse loginResponse = AuthResponse.builder().token(jwtToken).build();

        return ResponseEntity.ok(loginResponse);
    }
}
