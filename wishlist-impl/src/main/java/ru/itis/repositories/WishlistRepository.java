package ru.itis.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itis.entities.Wishlist;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    Optional<Wishlist> findByAuthorId(UUID authorId);

    @Query("SELECT w FROM Wishlist w WHERE w.author.id IN " +
           "(SELECT s.id.followingId FROM Subscription s WHERE s.id.followerId = :userId)")
    Slice<Wishlist> findSubscribedWishlistsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
