package ru.itis.services.notificationService.redis.smsCode;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
@Service
@RequiredArgsConstructor
public class RedisSmsCodeStorageService implements SmsCodeStorageService {

    private final StringRedisTemplate redisTemplate;

    private String codeKey(String phone) {
        return "sms:code:" + phone;
    }

    //попыток ввести код
    private String attemptsKey(String phone) {
        return "sms:attempts:" + phone;
    }

    @Override
    public void saveCode(String phone, String code, Duration ttl) {
        redisTemplate.opsForValue().set(codeKey(phone), code, ttl);
    }

    @Override
    public String getCode(String phone) {
        return redisTemplate.opsForValue().get(codeKey(phone));
    }

    @Override
    public void deleteCode(String phone) {
        redisTemplate.delete(codeKey(phone));
    }

    @Override
    public Long incrementAttempts(String phone, Duration ttl) {
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey(phone));
        redisTemplate.expire(attemptsKey(phone), ttl);
        return attempts;
    }

    @Override
    public void deleteAttempts(String phone) {
        redisTemplate.delete(attemptsKey(phone));
    }
}
