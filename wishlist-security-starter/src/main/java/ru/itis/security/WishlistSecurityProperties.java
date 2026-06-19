package ru.itis.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "wishlist.security")
public class WishlistSecurityProperties {
    private List<String> permitAll = new ArrayList<>(List.of("/actuator/health",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"));

    private List<String> requireAuth = new ArrayList<>(List.of("/api/**"));
}
