package ru.itis.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(
        @NotBlank(message = "Username не может быть пустым")
        @Size(min = 3, max = 50, message = "Username должен быть от 3 до 50 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username может содержать только латинские буквы, цифры и '_'")
        String username
) {
}
