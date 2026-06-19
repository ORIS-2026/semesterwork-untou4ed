package ru.itis.services.subscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.entities.Subscription;
import ru.itis.entities.SubscriptionId;
import ru.itis.entities.User;
import ru.itis.exceptions.AlreadySubscribedException;
import ru.itis.exceptions.NotSubscribedException;
import ru.itis.mappers.UserMapper;
import ru.itis.repositories.SubscriptionRepository;
import ru.itis.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void subscribe(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Нельзя подписаться на себя");
        }
        if (subscriptionRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId)) {
            throw new AlreadySubscribedException("Вы уже подписаны на этого пользователя");
        }

        User follower = userRepository.getReferenceById(followerId);
        User following = userRepository.getReferenceById(followingId);

        Subscription subscription = Subscription.builder()
                .id(new SubscriptionId(followerId, followingId))
                .follower(follower)
                .following(following)
                .build();

        subscriptionRepository.save(subscription);
        log.info("Пользователь {} подписался на {}", followerId, followingId);
    }

    @Override
    @Transactional
    public void unsubscribe(UUID followerId, UUID followingId) {
        if (!subscriptionRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId)) {
            throw new NotSubscribedException("Вы не подписаны на этого пользователя");
        }

        subscriptionRepository.deleteByIdFollowerIdAndIdFollowingId(followerId, followingId);
        log.info("Пользователь {} отписался от {}", followerId, followingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getMySubscriptions(UUID followerId) {
        return subscriptionRepository.findByIdFollowerId(followerId)
                .stream()
                .map(sub -> userMapper.toUserResponse(sub.getFollowing()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getMyFollowers(UUID userId) {
        return userRepository.findFollowers(userId)
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}
