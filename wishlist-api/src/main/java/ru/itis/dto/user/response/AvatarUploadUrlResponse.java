package ru.itis.dto.user.response;

import java.util.Map;

public record AvatarUploadUrlResponse(String uploadUrl, Map<String, String> formData, int ttlMinutes) {}
