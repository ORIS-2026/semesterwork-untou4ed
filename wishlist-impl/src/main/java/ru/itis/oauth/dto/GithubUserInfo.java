package ru.itis.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubUserInfo {

    private long id;

    private String login;

    private String name;

    private String email;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
