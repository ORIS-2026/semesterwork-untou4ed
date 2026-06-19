package ru.itis.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verification_codes")
public class PhoneVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private String code;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "used")
    private Boolean isUsed;

    private int attempts;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PhoneVerificationCode that = (PhoneVerificationCode) object;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "code: %s".formatted(code);
    }
}