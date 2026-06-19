package ru.itis.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CodeRequest {
    private String registrationId;
}
