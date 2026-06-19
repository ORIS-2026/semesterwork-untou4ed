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
public class GroupAccountId implements Serializable {

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "account_id")
    private UUID accountId;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        GroupAccountId that = (GroupAccountId) object;
        return Objects.equals(groupId, that.groupId) && Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, accountId);
    }
}
