package ru.itis.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "gifts")
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String link;

    @Nullable
    @Column(precision = 16, scale = 2)
    private BigDecimal price;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "owner_type", nullable = false)
    private GiftOwnerType ownerType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Gift gift = (Gift) object;
        return Objects.equals(id, gift.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "id %s, title %s, ownerId %s, ownerType %s, price %s, createdAt %s, updatedAt %s"
                .formatted(id, title, ownerId, ownerType, price, createdAt, updatedAt);
    }
}
