package ru.itis.controllers;

import ru.itis.api.UserApi;
import ru.itis.dto.user.request.ConfirmEmailRequest;
import ru.itis.dto.user.request.UpdateEmailRequest;
import ru.itis.dto.user.request.UpdateUserProfileRequest;
import ru.itis.dto.user.request.UpdateUsernameRequest;
import ru.itis.dto.user.response.AvatarUploadUrlResponse;
import ru.itis.dto.user.response.UserMeResponse;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMeResponse> getMe(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @Override
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestBody UpdateUserProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AvatarUploadUrlResponse> getAvatarUploadUrl(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.generateAvatarUploadUrl(userId));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> getAvatarDownloadUrl(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.getAvatarDownloadUrl(userId));
    }

    @Override
    public ResponseEntity<String> getUserAvatarDownloadUrl(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getAvatarDownloadUrl(UUID.fromString(userId)));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.deleteMe(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMeResponse> updateUsername(@AuthenticationPrincipal Jwt jwt,
                                                         @Valid @RequestBody UpdateUsernameRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.updateUsername(userId, request.username()));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> requestEmailChange(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody UpdateEmailRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.requestEmailChange(userId, request.email());
        return ResponseEntity.accepted().build();
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMeResponse> confirmEmailChange(@AuthenticationPrincipal Jwt jwt,
                                                             @Valid @RequestBody ConfirmEmailRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.confirmEmailChange(userId, request.code()));
    }
}
