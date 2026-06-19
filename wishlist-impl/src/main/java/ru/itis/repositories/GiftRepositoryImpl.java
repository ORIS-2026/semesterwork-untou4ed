package ru.itis.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GiftRepositoryImpl implements GiftRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Gift> searchGifts(String title, BigDecimal minPrice, BigDecimal maxPrice, GiftOwnerType ownerType) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Gift> cq = cb.createQuery(Gift.class);
        Root<Gift> root = cq.from(Gift.class);

        List<Predicate> predicates = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (ownerType != null) {
            predicates.add(cb.equal(root.get("ownerType"), ownerType));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("createdAt")));

        return em.createQuery(cq).getResultList();
    }
}
