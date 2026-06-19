package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.entities.Subscription;
import ru.itis.entities.SubscriptionId;
import ru.itis.entities.User;
import ru.itis.exceptions.AlreadySubscribedException;
import ru.itis.exceptions.NotSubscribedException;
import ru.itis.mappers.UserMapper;
import ru.itis.repositories.SubscriptionRepository;
import ru.itis.repositories.UserRepository;
import ru.itis.services.subscriptionService.SubscriptionServiceImpl;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @InjectMocks SubscriptionServiceImpl subscriptionService;

    private User user(UUID id) {
        return User.builder().id(id).username("u" + id).name("Name").enabled(true).phoneNumber("+7900").build();
    }

    @Test
    void subscribe_success() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(subscriptionRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId)).thenReturn(false);
        when(userRepository.getReferenceById(followerId)).thenReturn(user(followerId));
        when(userRepository.getReferenceById(followingId)).thenReturn(user(followingId));

        subscriptionService.subscribe(followerId, followingId);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void subscribe_toSelf_throws() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> subscriptionService.subscribe(id, id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("себя");
    }

    @Test
    void subscribe_alreadySubscribed_throws() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(subscriptionRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId)).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.subscribe(followerId, followingId))
                .isInstanceOf(AlreadySubscribedException.class);
    }

    @Test
    void unsubscribe_success() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(subscriptionRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId)).thenReturn(true);

        subscriptionService.unsubscribe(followerId, followingId);

        verify(subscriptionRepository).deleteByIdFollowerIdAndIdFollowingId(followerId, followingId);
    }

    @Test
    void unsubscribe_notSubscribed_throws() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(subscriptionRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.unsubscribe(followerId, followingId))
                .isInstanceOf(NotSubscribedException.class);
    }

    @Test
    void getMySubscriptions_returnsMappedUsers() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();
        User following = user(followingId);

        Subscription sub = Subscription.builder()
                .id(new SubscriptionId(followerId, followingId))
                .follower(user(followerId))
                .following(following)
                .build();

        UserResponse mapped = UserResponse.builder().id(followingId).username(following.getUsername()).build();

        when(subscriptionRepository.findByIdFollowerId(followerId)).thenReturn(List.of(sub));
        when(userMapper.toUserResponse(following)).thenReturn(mapped);

        List<UserResponse> result = subscriptionService.getMySubscriptions(followerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(followingId);
    }

    @Test
    void getMySubscriptions_empty() {
        UUID followerId = UUID.randomUUID();
        when(subscriptionRepository.findByIdFollowerId(followerId)).thenReturn(List.of());

        List<UserResponse> result = subscriptionService.getMySubscriptions(followerId);

        assertThat(result).isEmpty();
    }
}
