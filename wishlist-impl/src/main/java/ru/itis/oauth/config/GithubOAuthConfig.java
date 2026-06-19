package ru.itis.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubOAuthConfig {

    @Bean("githubRestClient")
    public RestClient githubRestClient() {
        return RestClient.builder()
                .baseUrl("https://github.com")
                .build();
    }

    @Bean("githubApiRestClient")
    public RestClient githubApiRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }
}
