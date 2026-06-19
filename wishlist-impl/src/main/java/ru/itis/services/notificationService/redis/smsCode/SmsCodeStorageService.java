package ru.itis.services.notificationService.redis.smsCode;

import java.time.Duration;

public interface SmsCodeStorageService {
    void saveCode(String phone, String code, Duration ttl);

    String getCode(String phone);

    void deleteCode(String phone);

    Long incrementAttempts(String phone, Duration ttl);

    void deleteAttempts(String phone);

}
