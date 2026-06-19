package ru.itis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itis.entities.GroupAccount;
import ru.itis.entities.GroupAccountId;
import ru.itis.entities.GroupMemberStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupAccountRepository extends JpaRepository<GroupAccount, GroupAccountId> {

    List<GroupAccount> findByIdGroupId(UUID groupId);

    Optional<GroupAccount> findByIdGroupIdAndIdAccountId(UUID groupId, UUID accountId);

    boolean existsByIdGroupIdAndIdAccountId(UUID groupId, UUID accountId);

    void deleteByIdGroupIdAndIdAccountId(UUID groupId, UUID accountId);

    @Query("SELECT ga.status FROM GroupAccount ga WHERE ga.id.groupId = :groupId AND ga.id.accountId = :accountId")
    Optional<GroupMemberStatus> findStatusByGroupIdAndAccountId(
            @Param("groupId") UUID groupId,
            @Param("accountId") UUID accountId
    );
}
