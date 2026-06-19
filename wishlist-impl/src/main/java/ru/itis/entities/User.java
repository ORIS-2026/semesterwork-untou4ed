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
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Nullable
    private String surname;

    @Column(nullable = false)
    private String username;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Nullable
    private String email;

    @Column(nullable = false)
    private Boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    @Nullable
    private Instant deletedAt;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "id %s, Name %s, surname %s, username %s, phoneNumber %s, enabled %s, avatarUrl %s, email %s, createdAt %s, updatedAt %s, deletedAt %s"
                .formatted(id, name, surname, username,
                        phoneNumber, enabled, avatarUrl, email, createdAt, updatedAt, deletedAt );
    }
}
