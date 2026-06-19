package ru.itis.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cache.redis")
public class RedisCacheProperties {

    private Duration defaultTtl;

    private String keyPrefix;

    private Map<String, Duration> ttl = new HashMap<>();
}