package ru.itis.oauth.service;

import ru.itis.oauth.dto.OAuthTokenResult;

public interface GithubOAuthService {

    String buildAuthorizationUrl(String state);

    OAuthTokenResult handleCallback(String code, String state, String expectedState);
}
