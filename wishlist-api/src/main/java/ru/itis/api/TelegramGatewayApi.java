package ru.itis.api;

public interface TelegramGatewayApi {

    /**
     * Отправить код верификации пользователю через Telegram.
     *
     * @param phoneNumber номер в формате E.164
     * @param code        числовой код (например "12345")
     * @param ttlSeconds  время жизни кода в секундах
     */
    void sendCode(String phoneNumber, String code, int ttlSeconds);
}
