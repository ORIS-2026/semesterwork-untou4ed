package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MinioException extends BusinessException {
    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
}
