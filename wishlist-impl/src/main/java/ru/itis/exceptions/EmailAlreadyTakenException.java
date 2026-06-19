package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyTakenException extends BusinessException {
    public EmailAlreadyTakenException(String message) {
        super(message);
    }
}
