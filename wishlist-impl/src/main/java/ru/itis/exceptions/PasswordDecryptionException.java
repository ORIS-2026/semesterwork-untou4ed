package ru.itis.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PasswordDecryptionException extends BusinessException {
    public PasswordDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}