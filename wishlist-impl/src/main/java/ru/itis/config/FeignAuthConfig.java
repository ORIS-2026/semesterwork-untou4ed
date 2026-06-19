package ru.itis.config;

import ru.itis.services.keycloak.KeycloakTokenService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeignAuthConfig {
    private final KeycloakTokenService tokenService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = tokenService.getToken();
            log.info("Запрос с существующим токеном {}", token != null && !token.isBlank());
            requestTemplate.header("Authorization", "Bearer %s".formatted(token));
        };
    }
}
