package ru.itis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.entities.Subscription;
import ru.itis.entities.SubscriptionId;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

    List<Subscription> findByIdFollowerId(UUID followerId);

    boolean existsByIdFollowerIdAndIdFollowingId(UUID followerId, UUID followingId);

    @Transactional
    void deleteByIdFollowerIdAndIdFollowingId(UUID followerId, UUID followingId);
}
