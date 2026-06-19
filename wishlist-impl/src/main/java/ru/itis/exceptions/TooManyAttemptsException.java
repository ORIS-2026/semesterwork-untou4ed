package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyAttemptsException extends BusinessException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}
