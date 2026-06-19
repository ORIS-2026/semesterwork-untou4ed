package ru.itis.dto.user.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(
        @NotBlank
        String code
) {
}
