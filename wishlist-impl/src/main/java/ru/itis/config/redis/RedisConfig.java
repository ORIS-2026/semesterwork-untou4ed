package ru.itis.config.redis;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class RedisConfig {

    @Bean
    public GenericJacksonJsonRedisSerializer redisSerializer() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .activateDefaultTypingAsProperty(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType(Object.class)
                                .build(),
                        DefaultTyping.NON_FINAL,
                        "@class"
                )
                .build();
        return new GenericJacksonJsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       GenericJacksonJsonRedisSerializer redisSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
