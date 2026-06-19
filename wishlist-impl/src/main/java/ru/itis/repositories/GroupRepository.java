package ru.itis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itis.entities.Group;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Query("SELECT ga.group FROM GroupAccount ga WHERE ga.account.id = :userId")
    List<Group> findGroupsByUserId(@Param("userId") UUID userId);

    List<Group> findByNameContainingIgnoreCase(String query);
}
