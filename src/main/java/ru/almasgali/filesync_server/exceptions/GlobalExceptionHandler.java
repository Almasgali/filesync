package ru.almasgali.filesync_server.exceptions;

import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.almasgali.filesync_server.exceptions.auth.AuthException;
import ru.almasgali.filesync_server.exceptions.auth.InvalidTokenException;
import ru.almasgali.filesync_server.exceptions.auth.TokenNotFoundException;
import ru.almasgali.filesync_server.exceptions.auth.UserAlreadyExistsException;
import ru.almasgali.filesync_server.exceptions.common.ConstraintViolationException;
import ru.almasgali.filesync_server.exceptions.storage.FileNotFoundException;
import ru.almasgali.filesync_server.exceptions.storage.OldVersionException;
import ru.almasgali.filesync_server.exceptions.storage.StorageException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(FileNotFoundException.class)
    public ProblemDetail catchResourceNotFoundException(FileNotFoundException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    public ProblemDetail catchStorageException(StorageException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail catchUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ProblemDetail catchAuthException(AuthException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ProblemDetail catchTokenNotFoundException(TokenNotFoundException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail catchInvalidTokenException(InvalidTokenException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail catchUsernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail catchBadCredentialsException(BadCredentialsException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ProblemDetail catchMalformedJwtException(MalformedJwtException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(io.jsonwebtoken.security.SecurityException.class)
    public ProblemDetail catchJWTSecurityException(io.jsonwebtoken.security.SecurityException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ProblemDetail catchSecurityException(SecurityException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail catchMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail catchConstraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail catchRuntimeException(RuntimeException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail catchHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(OldVersionException.class)
    public ProblemDetail catchOldVersionException (OldVersionException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail catchMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        Map<String, Object> errorsBody = Map.of("errors", errors);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setProperties(errorsBody);
        return pd;
    }
}
