package ru.itis.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import ru.itis.entities.Role;
import ru.itis.entities.User;
import ru.itis.oauth.dto.GithubTokenResponse;
import ru.itis.oauth.dto.GithubUserInfo;
import ru.itis.oauth.dto.OAuthTokenResult;
import ru.itis.oauth.properties.GithubOAuthProperties;
import ru.itis.properties.KeycloakProperties;
import ru.itis.repositories.UserRepository;
import ru.itis.services.keycloak.KeycloakAdminService;
import ru.itis.services.wishlistService.WishlistService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubOAuthServiceImpl implements GithubOAuthService {

    private static final String GITHUB_AUTH_URL   = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_PATH = "/login/oauth/access_token";
    private static final String GITHUB_USER_PATH  = "/user";

    private final GithubOAuthProperties githubProps;
    private final KeycloakProperties    keycloakProps;
    private final KeycloakAdminService  keycloakAdminService;
    private final UserRepository        userRepository;
    private final WishlistService       wishlistService;

    // сеттер-инъекция т.к. @RequiredArgsConstructor не копирует @Qualifier в параметры конструктора
    @Setter(onMethod_ = {@Autowired, @Qualifier("githubRestClient")})
    private RestClient githubRestClient;

    @Setter(onMethod_ = {@Autowired, @Qualifier("githubApiRestClient")})
    private RestClient githubApiRestClient;

    // строим URL для редиректа на GitHub
    @Override
    public String buildAuthorizationUrl(String state) {
        return GITHUB_AUTH_URL
                + "?client_id="    + githubProps.getClientId()
                + "&redirect_uri=" + githubProps.getRedirectUri()
                + "&scope=user:email"
                + "&state="        + state
                + "&response_type=code";
    }

    // обработать callback от GitHub
    @Override
    @Transactional
    public OAuthTokenResult handleCallback(String code, String state, String expectedState) {
        if (!state.equals(expectedState)) {
            throw new IllegalStateException("Недействительный state: возможная CSRF-атака");
        }

        // обмениваем code на GitHub access_token
        String githubAccessToken = exchangeCodeForToken(code);
        log.info("Получен GitHub access_token");

        // получаем данные пользователя GitHub
        GithubUserInfo userInfo = fetchGithubUserInfo(githubAccessToken);
        log.info("GitHub user: id={}, login={}", userInfo.getId(), userInfo.getLogin());

        // находим или создаём пользователя в нашей системе
        String username = "github_" + userInfo.getId();
        User user = findOrCreateUser(username, userInfo);

        return getKeycloakToken(username, derivePassword(userInfo.getId()));
    }

    // обмен code на gitHub access_token
    private String exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id",     githubProps.getClientId());
        form.add("client_secret", githubProps.getClientSecret());
        form.add("code",          code);
        form.add("redirect_uri",  githubProps.getRedirectUri());

        GithubTokenResponse response = githubRestClient.post()
                .uri(GITHUB_TOKEN_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(GithubTokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("GitHub не вернул access_token");
        }

        return response.getAccessToken();
    }

    // получение профиля пользователя GitHub ──────────────────────────
    private GithubUserInfo fetchGithubUserInfo(String accessToken) {
        GithubUserInfo userInfo = githubApiRestClient.get()
                .uri(GITHUB_USER_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(GithubUserInfo.class);

        if (userInfo == null) {
            throw new IllegalStateException("Не удалось получить данные пользователя GitHub");
        }

        return userInfo;
    }

    // находим или создаем пользователя
    private User findOrCreateUser(String username, GithubUserInfo userInfo) {
        Optional<User> existing = userRepository.findByUsernameAndDeletedAtIsNull(username);
        if (existing.isPresent()) {
            log.info("GitHub пользователь уже существует: {}", username);
            return existing.get();
        }

        log.info("Создаём нового пользователя для GitHub аккаунта: {}", userInfo.getLogin());

        // данные для обязательных полей
        String syntheticPhone = "gh_" + userInfo.getId();
        String email          = (userInfo.getEmail() != null && !userInfo.getEmail().isBlank())
                ? userInfo.getEmail()
                : "github_" + userInfo.getId() + "@oauth.local";
        String password       = derivePassword(userInfo.getId());

        // создаём пользователя в Keycloak
        UUID keycloakId = keycloakAdminService.createUser(username, syntheticPhone, password);
        keycloakAdminService.assignRealmRole(keycloakId, Role.USER.getValue());

        // Разбиваем GitHub name на имя и фамилию
        String[] nameParts = splitName(userInfo.getName(), userInfo.getLogin());

        // Создаём пользователя в нашей БД
        User user = User.builder()
                .id(keycloakId)
                .username(username)
                .name(nameParts[0])
                .surname(nameParts[1])
                .phoneNumber(syntheticPhone)
                .email(email)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.saveAndFlush(user);
        wishlistService.createUserWishlist(user);

        log.info("Пользователь {} создан через GitHub OAuth", username);
        return user;
    }

    // получаем Keycloak JWT для пользователя
    private OAuthTokenResult getKeycloakToken(String username, String password) {
        String tokenUrl = keycloakProps.getUrl()
                + "/realms/" + keycloakProps.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id",  keycloakProps.getFrontendClientId());
        form.add("username",   username);
        form.add("password",   password);

        RestClient keycloakClient = RestClient.builder().build();

        Map<?, ?> response = keycloakClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Keycloak не вернул токен");
        }

        return new OAuthTokenResult(
                (String) response.get("access_token"),
                (String) response.get("refresh_token")
        );
    }

    //генерирует пароль для OAuth пользователя на основе его GitHub ID
    private String derivePassword(long githubId) {
        String input = "github:" + githubId + ":" + githubProps.getClientSecret();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // берём только первые 16 байт и добавляем префикс чтобы кейклок не отклонил пароль
            return "Gh" + HexFormat.of().formatHex(hash).substring(0, 30);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }

    private String[] splitName(String fullName, String fallback) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{fallback, ""};
        }
        int space = fullName.indexOf(' ');
        if (space == -1) {
            return new String[]{fullName, ""};
        }
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1)};
    }
}
