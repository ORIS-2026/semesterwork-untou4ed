package ru.itis.dto.auth.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class EqualsPasswordValidator implements ConstraintValidator<EqualsPasswords, Object> {

    private String passwordField;
    private String passwordRepeatField;

    @Override
    public void initialize(EqualsPasswords constraintAnnotation) {
        passwordField = constraintAnnotation.password();
        passwordRepeatField = constraintAnnotation.passwordRepeat();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        Object password = new BeanWrapperImpl(object).getPropertyValue(passwordField);
        Object passwordRepeat = new BeanWrapperImpl(object).getPropertyValue(passwordRepeatField);

        boolean valid = password != null && password.equals(passwordRepeat);

        if (!valid) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate("Пароли не совпадают")
                    .addPropertyNode(passwordRepeatField)
                    .addConstraintViolation();
        }

        return valid;
    }
}