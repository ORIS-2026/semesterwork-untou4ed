package ru.itis.dto.telegram.response;

import lombok.Data;

@Data
public class TelegramApiResponse<T> {

    private boolean ok;
    private T result;
    private String error;
}
