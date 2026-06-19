package ru.itis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionId implements Serializable {

    @Column(name = "follower_id")
    private UUID followerId;

    @Column(name = "following_id")
    private UUID followingId;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SubscriptionId that = (SubscriptionId) object;
        return Objects.equals(followerId, that.followerId) && Objects.equals(followingId, that.followingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, followingId);
    }
}
