package ru.itis.notifications.telegram.client;

import ru.itis.dto.telegram.response.RequestStatus;

public interface TelegramGatewayClient {

    //отправить код верификации на номер телефона через Telegram Gateway
    RequestStatus sendVerificationMessage(String phoneNumber, String code, int ttlSeconds);

    //проверить, зарегистрирован ли номер в Telegram
    RequestStatus checkSendAbility(String phoneNumber);

    // можно отозвать сообщение до истечения TTL
    boolean revokeVerificationMessage(String requestId);
}
