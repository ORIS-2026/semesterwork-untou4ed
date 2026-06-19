package ru.itis.dto.auth.request;

import ru.itis.dto.auth.validators.EqualsPasswords;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsPasswords(message = "Пароли не совпадают", password = "password", passwordRepeat = "passwordRepeat")
public class RegisterRequest {

    @NotBlank(message = "Имя не может быть пустым.")
    @Size(max = 50, message = "Имя должно содержать не больше 50 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё]+$",
            message = "Имя может содержать только русские или английские буквы"
    )
    private String name;

    @Size(max = 50, message = "Фамилия должна содержать не больше 50 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё]*$",
            message = "Фамилия может содержать только русские или английские буквы"
    )
    private String surname;

    @NotBlank(message = "Имя пользователя не может быть пустым.")
    @Size(max = 50, message = "Имя пользователя должно содержать не больше 50 символов")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Username может содержать только английские буквы, цифры и символ '_'"
    )
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Pattern(
            regexp = "^(?=[A-Za-z])(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z][A-Za-z0-9]*$",
            message = "Пароль должен содержать заглавные и строчные буквы, цифры и начинаться на букву"
    )
    @Size(min = 8, message = "Пароль должен состоять минимум из 8 символов")
    private String password;

    private String passwordRepeat;

    @NotBlank(message = "Номер телефона не может быть пустым")
    @Pattern(
            regexp = "^\\+?[0-9]{11}$",
            message = "Номер должен содержать ровно 11 цифр и может начинаться с +"
    )
    private String number;
}