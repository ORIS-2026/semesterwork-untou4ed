package ru.itis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeycloakUserRepresentation{
        private String id;
        private String username;
        private String email;
        private Boolean emailVerified;
        private Boolean enabled;
        private Map<String, Object> attributes;
}