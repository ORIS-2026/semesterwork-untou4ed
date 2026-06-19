package ru.itis.services;

import ru.itis.dto.user.request.UpdateUserProfileRequest;
import ru.itis.dto.user.response.AvatarUploadUrlResponse;
import ru.itis.dto.user.response.UserAdminResponse;
import ru.itis.dto.user.response.UserMeResponse;
import ru.itis.dto.user.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {
    UserMeResponse getMe(UUID userId);

    Page<UserAdminResponse> findAllUsers(int page, int size, Boolean enabled, boolean includeDeleted);

    UserAdminResponse blockUser(UUID userId);

    UserAdminResponse unblockUser(UUID userId);

    void requestEmailChange(UUID userId, String email);

    UserMeResponse confirmEmailChange(UUID userId, String code);

    UserResponse findById(String userId);

    UserResponse findByUsername(String username);

    UserResponse updateProfile(UUID userId, UpdateUserProfileRequest request);

    UserMeResponse updateUsername(UUID userId, String username);

    AvatarUploadUrlResponse generateAvatarUploadUrl(UUID userId);

    String getAvatarDownloadUrl(UUID userId);

    void deleteMe(UUID userId);
}
