package ru.itis.config.redis;

import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import ru.itis.properties.RedisCacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig {
    private final RedisCacheProperties redisCacheProperties;

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                               GenericJacksonJsonRedisSerializer redisSerializer) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(redisCacheProperties.getDefaultTtl())
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                redisSerializer
                        )
                )
                .computePrefixWith(cacheName -> "%s%s:".formatted(redisCacheProperties.getKeyPrefix(), cacheName));

        Map<String, RedisCacheConfiguration> configs = redisCacheProperties.getTtl()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> defaultConfig.entryTtl(entry.getValue())
                ));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }
}