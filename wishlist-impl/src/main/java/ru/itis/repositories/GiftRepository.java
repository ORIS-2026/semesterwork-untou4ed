package ru.itis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;

import java.util.List;
import java.util.UUID;

@Repository
public interface GiftRepository extends JpaRepository<Gift, UUID>, GiftRepositoryCustom {

    List<Gift> findByOwnerIdAndOwnerType(UUID ownerId, GiftOwnerType ownerType);

    // решаем  N+1
    List<Gift> findByOwnerIdInAndOwnerType(List<UUID> ownerIds, GiftOwnerType ownerType);
}
