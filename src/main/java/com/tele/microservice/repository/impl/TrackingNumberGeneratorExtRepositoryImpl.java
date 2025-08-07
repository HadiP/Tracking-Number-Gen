package com.tele.microservice.repository.impl;

import com.tele.microservice.repository.TrackingNumberGeneratorExtRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

public class TrackingNumberGeneratorExtRepositoryImpl implements TrackingNumberGeneratorExtRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pair<Long, Long> reserveBlock(String origin) {
        Query q = entityManager.createNativeQuery("""
                UPDATE tracking_id_generator
                SET next_value = next_value + block_size
                WHERE origin_code = :origin
                RETURNING next_value - block_size, next_value
                """);
        q.setParameter("origin", origin);

        Object[] result = (Object[]) q.getSingleResult();
        long start = ((Number) result[0]).longValue();
        long end = ((Number) result[1]).longValue();

        return Pair.of(start, end);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pair<Long, Long> resetSequence(String origin, OffsetDateTime resetDate) {
        Query q = entityManager.createNativeQuery("""
                UPDATE tracking_id_generator
                   SET next_value = 1,
                       reset_times = reset_times + 1,
                       last_reset_timestamp = :resetDate
                   WHERE origin_code = :origin
                   RETURNING next_value, block_size - next_value
                """);
        q.setParameter("origin", origin);
        q.setParameter("resetDate", resetDate);

        Object[] result = (Object[]) q.getSingleResult();
        long start = ((Number) result[0]).longValue();
        long end = ((Number) result[1]).longValue();

        return Pair.of(start, end);
    }
}
