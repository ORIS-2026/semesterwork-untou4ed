package ru.itis.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itis.entities.User;

import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByIdAndDeletedAtIsNull(UUID userId);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    boolean existsUserByUsernameAndDeletedAtIsNull(String username);

    boolean existsUserByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    boolean existsUserByEmailAndDeletedAtIsNull(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:includeDeleted = true OR u.deletedAt IS NULL)")
    Page<User> findAllWithFilters(
            @Param("enabled") Boolean enabled,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchUsers(@Param("query") String query);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.id IN " +
           "(SELECT s.id.followerId FROM Subscription s WHERE s.id.followingId = :userId)")
    List<User> findFollowers(@Param("userId") UUID userId);
}
