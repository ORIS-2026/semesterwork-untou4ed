package ru.itis.services.notificationService.redis.smsCode;

import ru.itis.properties.CodeProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final CodeProperty codeProperty;

    public boolean allow(String key, DefaultRedisScript<Long> script) {

        String cooldownKey = "sms:cooldown:" + key;
        String windowKey = "sms:window:" + key;

        Long result = redisTemplate.execute(
                script,
                List.of(cooldownKey, windowKey),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(codeProperty.getSingleSmsCooldown() * 60),  // минуты → секунды
                String.valueOf(codeProperty.getMaxSmsInTotalTime()),
                String.valueOf(codeProperty.getTotalTime() * 60)           // минуты → секунды
        );

        return result != null && result == 1;
    }
}