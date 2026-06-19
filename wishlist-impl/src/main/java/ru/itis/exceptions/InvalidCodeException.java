package ru.itis.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCodeException extends BusinessException {
    public InvalidCodeException(String message) {
        super(message);
    }
}
