package ru.itis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "telegram.gateway")
public class TelegramGatewayProperties {

    private String token;

    private String baseUrl = "https://gatewayapi.telegram.org";
}
