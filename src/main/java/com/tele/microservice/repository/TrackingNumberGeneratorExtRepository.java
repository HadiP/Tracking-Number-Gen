package com.tele.microservice.repository;

import org.springframework.data.util.Pair;

import java.time.OffsetDateTime;

/**
 * This repository extension created to fix issue with the <code>Pair.class</code> return type.
 * Since JPA couldn't recognize Pair I need to make repository implementation to handle the return values.
 */
public interface TrackingNumberGeneratorExtRepository {

    /**
     * Reserve sequence block from DB then use queue to distribute the numbers.
     * @param origin
     * @return
     */
    Pair<Long, Long> reserveBlock(String origin);

    /**
     * Atomically reset the sequence counter and last_reset_date
     * for the given originCode.
     */
    Pair<Long, Long> resetSequence(
            String origin,
            OffsetDateTime resetDate);
}
