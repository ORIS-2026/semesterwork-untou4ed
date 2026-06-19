package ru.itis.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "wishlists")
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Builder.Default
    @Column(nullable = false)
    private String title = "Мой вишлист";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, unique = true)
    private User author;

    @Nullable
    private String description;

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
        Wishlist wishlist = (Wishlist) object;
        return Objects.equals(id, wishlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "id %s, title %s, authorId %s, description %s, createdAt %s, updatedAt %s"
                .formatted(id, title, author != null ? author.getId() : null, description, createdAt, updatedAt);
    }


    @PrePersist
    private void setDefaults() {
        if (title == null || title.isBlank()) {
            title = "Мой вишлист";
        }
    }
}
