package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyGroupMemberException extends BusinessException {
    public AlreadyGroupMemberException(String message) {
        super(message);
    }
}
