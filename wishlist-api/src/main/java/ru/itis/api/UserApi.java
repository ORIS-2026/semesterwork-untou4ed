package ru.itis.api;

import ru.itis.dto.user.request.ConfirmEmailRequest;
import ru.itis.dto.user.request.UpdateEmailRequest;
import ru.itis.dto.user.request.UpdateUserProfileRequest;
import ru.itis.dto.user.request.UpdateUsernameRequest;
import ru.itis.dto.user.response.AvatarUploadUrlResponse;
import ru.itis.dto.user.response.UserMeResponse;
import ru.itis.dto.user.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
public interface UserApi {
    @GetMapping("/me")
    ResponseEntity<UserMeResponse> getMe(Authentication authentication);

    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUserById(@PathVariable String userId);

    @GetMapping("/username/{username}")
    ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username);

    @PatchMapping("/me/profile")
    ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                               @RequestBody UpdateUserProfileRequest request);

    @GetMapping("/me/avatar/upload-url")
    ResponseEntity<AvatarUploadUrlResponse> getAvatarUploadUrl(@AuthenticationPrincipal Jwt jwt);

    @GetMapping("/me/avatar")
    ResponseEntity<String> getAvatarDownloadUrl(@AuthenticationPrincipal Jwt jwt);

    @GetMapping("/{userId}/avatar")
    ResponseEntity<String> getUserAvatarDownloadUrl(@PathVariable String userId);

    @DeleteMapping("/me")
    ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt);

    @PatchMapping("/me/username")
    ResponseEntity<UserMeResponse> updateUsername(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody UpdateUsernameRequest request);

    @PatchMapping("/me/email")
    ResponseEntity<Void> requestEmailChange(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody UpdateEmailRequest request);

    @PostMapping("/me/email/confirm")
    ResponseEntity<UserMeResponse> confirmEmailChange(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody ConfirmEmailRequest request);
}
