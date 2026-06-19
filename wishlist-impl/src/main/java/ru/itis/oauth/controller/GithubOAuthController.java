package ru.itis.oauth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import ru.itis.oauth.dto.OAuthTokenResult;
import ru.itis.oauth.service.GithubOAuthService;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Controller
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
public class GithubOAuthController {

    private static final String STATE_PREFIX = "oauth:state:";
    private static final Duration STATE_TTL  = Duration.ofMinutes(10);

    private final GithubOAuthService    githubOAuthService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();


    @GetMapping
    public RedirectView initiateOAuth(HttpServletRequest request) {
        log.info("Потзователь инициализировал вход через github");
        String state = generateState();

        // cохраняем state в Redis с ttl
        redisTemplate.opsForValue().set(STATE_PREFIX + state, "1", STATE_TTL);

        String authUrl = githubOAuthService.buildAuthorizationUrl(state);
        log.info("Редирект на GitHub OAuth: state={}", state);

        return new RedirectView(authUrl);
    }


     // GitHub редиректит обратно с code и state.
    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        // Проверяем state из Redis
        String storedState = (String) redisTemplate.opsForValue().get(STATE_PREFIX + state);
        if (storedState == null) {
            log.warn("OAuth callback: state не найден или истёк: {}", state);
            return new RedirectView("/login?error=oauth_state_invalid");
        }

        // удаляем использованный state
        redisTemplate.delete(STATE_PREFIX + state);

        OAuthTokenResult result;
        try {
            result = githubOAuthService.handleCallback(code, state, state);
        } catch (Exception e) {
            log.error("Ошибка OAuth callback: {}", e.getMessage(), e);
            return new RedirectView("/login?error=oauth_failed");
        }

        // передаём токены на фронтенд через URL
        String redirectUrl = "/oauth/success"
                + "?accessToken="  + result.getAccessToken()
                + "&refreshToken=" + result.getRefreshToken();

        return new RedirectView(redirectUrl);
    }

    private String generateState() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
