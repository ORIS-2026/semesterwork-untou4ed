package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.itis.dto.PendingEmailChange;
import ru.itis.dto.user.request.UpdateUserProfileRequest;
import ru.itis.dto.user.response.UserAdminResponse;
import ru.itis.dto.user.response.UserMeResponse;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.entities.User;
import ru.itis.exceptions.EmailAlreadyTakenException;
import ru.itis.exceptions.UserNotFoundException;
import ru.itis.exceptions.UsernameAlreadyTakenException;
import ru.itis.mappers.UserMapper;
import ru.itis.properties.MinioProperties;
import ru.itis.repositories.UserRepository;
import ru.itis.services.keycloak.KeycloakAdminService;
import ru.itis.services.minio.MinioStorageService;
import ru.itis.services.notificationService.redis.email.EmailVerificationStorageService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.Bean.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repository;
    @Mock UserMapper userMapper;
    @Mock MinioStorageService minioStorageService;
    @Mock MinioProperties minioProperties;
    @Mock KeycloakAdminService keycloakAdminService;
    @Mock EmailVerificationStorageService emailVerificationStorageService;
    @Mock org.springframework.context.ApplicationEventPublisher eventPublisher;
    @InjectMocks UserServiceImpl userService;

    private User activeUser(UUID id) {
        return User.builder().id(id).username("user1").name("Ivan").surname("Ivanov")
                .phoneNumber("+79001234567").enabled(true).build();
    }

    @Test
    void getMe_found() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UserMeResponse expected = UserMeResponse.builder().id(id).username("user1").build();

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(UserMapper.INSTANCE.toUserMeResponse(user)).thenReturn(expected);

        UserMeResponse result = userService.getMe(id);

        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void getMe_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findById_found() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UserResponse expected = UserResponse.builder().id(id).username("user1").build();

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.findById(id.toString());

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id.toString()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findByUsername_found() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UserResponse expected = UserResponse.builder().id(id).username("user1").build();

        when(repository.findByUsernameAndDeletedAtIsNull("user1")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.findByUsername("user1");

        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void findByUsername_notFound_throws() {
        when(repository.findByUsernameAndDeletedAtIsNull("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateProfile_updatesNameAndSurname() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UpdateUserProfileRequest req = new UpdateUserProfileRequest("Пётр", "Петров");
        UserResponse expected = UserResponse.builder().id(id).name("Пётр").build();

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.updateProfile(id, req);

        assertThat(user.getName()).isEqualTo("Пётр");
        assertThat(user.getSurname()).isEqualTo("Петров");
    }

    @Test
    void updateProfile_userNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(id, new UpdateUserProfileRequest("x", "y")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteMe_setsDeletedAt() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));

        userService.deleteMe(id);

        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteMe_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteMe(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void blockUser_blocksActiveUser() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UserAdminResponse expected = UserAdminResponse.builder().id(id).enabled(false).build();

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserAdminResponse(user)).thenReturn(expected);

        UserAdminResponse result = userService.blockUser(id);

        assertThat(user.getEnabled()).isFalse();
    }

    @Test
    void blockUser_alreadyBlocked_throws() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        user.setEnabled(false);

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.blockUser(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже заблокирован");
    }

    @Test
    void unblockUser_unblocks() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        user.setEnabled(false);
        UserAdminResponse expected = UserAdminResponse.builder().id(id).enabled(true).build();

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserAdminResponse(user)).thenReturn(expected);

        userService.unblockUser(id);

        assertThat(user.getEnabled()).isTrue();
    }

    @Test
    void unblockUser_alreadyActive_throws() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.unblockUser(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже активен");
    }

    @Test
    void updateUsername_takenUsername_throws() {
        UUID id = UUID.randomUUID();
        when(repository.existsUserByUsernameAndDeletedAtIsNull("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUsername(id, "taken"))
                .isInstanceOf(UsernameAlreadyTakenException.class);
    }

    @Test
    void updateUsername_success() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UserMeResponse expected = UserMeResponse.builder().id(id).username("newname").build();

        when(repository.existsUserByUsernameAndDeletedAtIsNull("newname")).thenReturn(false);
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserMeResponse(user)).thenReturn(expected);

        UserMeResponse result = userService.updateUsername(id, "newname");

        assertThat(user.getUsername()).isEqualTo("newname");
        verify(keycloakAdminService).updateUsername(id.toString(), "newname");
    }

    @Test
    void requestEmailChange_emailTaken_throws() {
        UUID id = UUID.randomUUID();
        when(repository.existsUserByEmailAndDeletedAtIsNull("taken@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.requestEmailChange(id, "taken@mail.ru"))
                .isInstanceOf(EmailAlreadyTakenException.class);
    }

    @Test
    void requestEmailChange_savesCode() {
        UUID id = UUID.randomUUID();
        when(repository.existsUserByEmailAndDeletedAtIsNull("new@mail.ru")).thenReturn(false);

        userService.requestEmailChange(id, "new@mail.ru");

        verify(emailVerificationStorageService).save(eq(id), any(PendingEmailChange.class));
    }

    @Test
    void confirmEmailChange_wrongCode_throws() {
        UUID id = UUID.randomUUID();
        when(emailVerificationStorageService.find(id))
                .thenReturn(Optional.of(new PendingEmailChange("new@mail.ru", "123456")));

        assertThatThrownBy(() -> userService.confirmEmailChange(id, "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный код");
    }

    @Test
    void confirmEmailChange_emailAlreadyTaken_throws() {
        UUID id = UUID.randomUUID();
        when(emailVerificationStorageService.find(id))
                .thenReturn(Optional.of(new PendingEmailChange("taken@mail.ru", "123456")));
        when(repository.existsUserByEmailAndDeletedAtIsNull("taken@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.confirmEmailChange(id, "123456"))
                .isInstanceOf(EmailAlreadyTakenException.class);
    }

    @Test
    void confirmEmailChange_success() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        UserMeResponse expected = UserMeResponse.builder().id(id).build();

        when(emailVerificationStorageService.find(id))
                .thenReturn(Optional.of(new PendingEmailChange("new@mail.ru", "123456")));
        when(repository.existsUserByEmailAndDeletedAtIsNull("new@mail.ru")).thenReturn(false);
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserMeResponse(user)).thenReturn(expected);

        userService.confirmEmailChange(id, "123456");

        assertThat(user.getEmail()).isEqualTo("new@mail.ru");
        verify(keycloakAdminService).updateEmail(id.toString(), "new@mail.ru");
        verify(emailVerificationStorageService).delete(id);
    }

    @Test
    void getAvatarDownloadUrl_noAvatar_throws() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        user.setAvatarUrl(null);

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getAvatarDownloadUrl(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Аватар не найден");
    }

    @Test
    void getAvatarDownloadUrl_returnsUrl() {
        UUID id = UUID.randomUUID();
        User user = activeUser(id);
        user.setAvatarUrl("avatars/" + id);

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(user));
        when(minioProperties.getAvatarDownloadTtlMinutes()).thenReturn(60);
        when(minioStorageService.generateDownloadPresignedUrl("avatars/" + id, 60))
                .thenReturn("https://minio/avatars/" + id + "?token=xxx");

        String url = userService.getAvatarDownloadUrl(id);

        assertThat(url).startsWith("https://minio");
    }
}
