package ru.itis.services.keycloak;

import ru.itis.dto.auth.response.TokenResponse;
import ru.itis.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTokenService {

    private final RestClient restClient = RestClient.builder().build();

    private final KeycloakProperties keycloakProperties;

    private String token;
    private long expiresAt;

    public synchronized String getToken() {
        if (token == null || System.currentTimeMillis() > expiresAt) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", keycloakProperties.getClientId());
            form.add("client_secret", keycloakProperties.getClientSecret());

            StringBuilder sb = new StringBuilder();
            String url = sb.append(keycloakProperties.getUrl())
                    .append("/realms/")
                    .append(keycloakProperties.getRealm())
                    .append("/protocol/openid-connect/token")
                    .toString();

            TokenResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
            log.info("Token response {}", response);

            if (response == null || response.getAccessToken() == null) {
                log.error("Не удалось получить accessToken {}", response);
                throw new IllegalStateException("Keycloak did not return access_token");
            }

            token = response.getAccessToken();
            expiresAt = System.currentTimeMillis() + (response.getExpiresIn() - 30L) * 1000L;
        }

        return token;
    }
}