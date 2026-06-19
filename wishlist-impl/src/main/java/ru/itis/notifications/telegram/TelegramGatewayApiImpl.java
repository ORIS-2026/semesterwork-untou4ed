package ru.itis.notifications.telegram;

import ru.itis.api.TelegramGatewayApi;
import ru.itis.notifications.telegram.client.TelegramGatewayClient;
import ru.itis.dto.telegram.response.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramGatewayApiImpl implements TelegramGatewayApi {

    private final TelegramGatewayClient telegramGatewayClient;

    @Override
    public void sendCode(String phoneNumber, String code, int ttlSeconds) {
        log.info("Отправка кода верификации через Telegram Gateway на номер {}", phoneNumber);

        RequestStatus status = telegramGatewayClient.sendVerificationMessage(phoneNumber, code, ttlSeconds);

        log.info("Код отправлен через Telegram Gateway. request_id={}, phone={}, cost={}",
                status.getRequestId(),
                status.getPhoneNumber(),
                status.getRequestCost());
    }
}
