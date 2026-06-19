package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GroupNotFoundException extends BusinessException {
    public GroupNotFoundException(String message) {
        super(message);
    }
}
