package ru.itis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class TelegramGatewayException extends BusinessException {

    public TelegramGatewayException(String message) {
        super(message);
    }

    public TelegramGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
