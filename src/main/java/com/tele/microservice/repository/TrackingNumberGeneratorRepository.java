package com.tele.microservice.repository;

import com.tele.microservice.entity.TrackingNumberGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Repository
public interface TrackingNumberGeneratorRepository extends JpaRepository<TrackingNumberGenerator, String> {

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = """
            UPDATE tracking_id_generator
                    SET next_value = next_value + block_size
                    WHERE origin_code = :origin
                    RETURNING next_value - block_size, next_value
            """, nativeQuery = true)
    Pair<Long, Long> reserveBlock(@Param("origin") String origin);

    /**
     * Atomically reset the sequence counter and last_reset_date
     * for the given originCode.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = """
            UPDATE tracking_id_generator
               SET next_value = 1,
                   reset_times = reset_times + 1,
                   last_reset_timestamp = :resetDate
               WHERE origin_code = :origin
               RETURNING next_value, block_size - next_value
            """, nativeQuery = true)
    Pair<Long, Long> resetSequence(
            @Param("origin") String origin,
            @Param("resetDate") OffsetDateTime resetDate);

}
