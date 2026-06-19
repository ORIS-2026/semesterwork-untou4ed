package ru.itis.oauth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "github.oauth")
@Data
public class GithubOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
