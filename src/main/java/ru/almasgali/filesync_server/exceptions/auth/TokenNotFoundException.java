package ru.almasgali.filesync_server.exceptions.auth;

public class TokenNotFoundException extends AuthException {
    public TokenNotFoundException(String message) {
        super(message);
    }

    public TokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
