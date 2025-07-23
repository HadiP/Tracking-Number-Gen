package com.tele.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TrackingNumberGeneratorRepository extends JpaRepository<Object, Object> {

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value =
            "UPDATE tracking_id_generator " +
                    "SET next_value = next_value + block_size " +
                    "WHERE instance_name = :name " +
                    "RETURNING next_value - block_size, next_value",
            nativeQuery = true)
    Pair<Long, Long> reserveBlock(@Param("name") String insName);
}
