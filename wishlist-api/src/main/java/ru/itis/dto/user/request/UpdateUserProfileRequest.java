package ru.itis.dto.user.request;

public record UpdateUserProfileRequest(
        String name,
        String surname
) {}
