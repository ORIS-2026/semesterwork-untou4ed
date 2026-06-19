package ru.itis.notifications.telegram.client;

import ru.itis.exceptions.TelegramGatewayException;
import ru.itis.dto.telegram.request.SendVerificationRequest;
import ru.itis.dto.telegram.response.RequestStatus;
import ru.itis.dto.telegram.response.TelegramApiResponse;
import ru.itis.properties.TelegramGatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramGatewayClientImpl implements TelegramGatewayClient {

    private static final String SEND_URL    = "/sendVerificationMessage";
    private static final String ABILITY_URL = "/checkSendAbility";
    private static final String REVOKE_URL  = "/revokeVerificationMessage";

    private final RestClient telegramRestClient;
    private final TelegramGatewayProperties properties;

    @Override
    public RequestStatus sendVerificationMessage(String phoneNumber, String code, int ttlSeconds) {
        SendVerificationRequest body = SendVerificationRequest.builder()
                .phoneNumber(phoneNumber)
                .code(code)
                .ttl(ttlSeconds)
                .build();

        return post(SEND_URL, body, new ParameterizedTypeReference<TelegramApiResponse<RequestStatus>>() {}).getResult();
    }

    @Override
    public RequestStatus checkSendAbility(String phoneNumber) {
        return post(ABILITY_URL,
                Map.of("phone_number", phoneNumber),
                new ParameterizedTypeReference<TelegramApiResponse<RequestStatus>>() {}).getResult();
    }

    @Override
    public boolean revokeVerificationMessage(String requestId) {
        return post(REVOKE_URL,
                Map.of("request_id", requestId),
                new ParameterizedTypeReference<TelegramApiResponse<Boolean>>() {}).getResult();
    }

    private <T> TelegramApiResponse<T> post(String path,
                                             Object body,
                                             ParameterizedTypeReference<TelegramApiResponse<T>> responseType) {
        try {
            TelegramApiResponse<T> response = telegramRestClient.post()
                    .uri(properties.getBaseUrl() + path)
                    .header("Authorization", "Bearer " + properties.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(responseType);

            if (response == null || !response.isOk()) {
                String error = response != null ? response.getError() : "пустой ответ";
                log.error("Telegram Gateway вернул ошибку для {}: {}", path, error);
                throw new TelegramGatewayException("Telegram Gateway error: " + error);
            }

            return response;

        } catch (RestClientException e) {
            log.error("Ошибка HTTP при обращении к Telegram Gateway {}: {}", path, e.getMessage());
            throw new TelegramGatewayException("Не удалось связаться с Telegram Gateway", e);
        }
    }
}
