package ru.itis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TelegramConfig {

    @Bean
    public RestClient telegramRestClient() {
        return RestClient.builder().build();
    }
}
