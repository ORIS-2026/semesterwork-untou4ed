package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyTakenException extends BusinessException {
    public UsernameAlreadyTakenException(String message) {
        super(message);
    }
}
