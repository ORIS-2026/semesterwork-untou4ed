package ru.itis.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itis.entities.Compilation;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, UUID> {

    List<Compilation> findByGroupId(UUID groupId);

    @Query("SELECT c FROM Compilation c WHERE c.group.id IN " +
           "(SELECT ga.id.groupId FROM GroupAccount ga WHERE ga.id.accountId = :userId)")
    Slice<Compilation> findCompilationsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
