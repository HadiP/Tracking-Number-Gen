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
public interface TrackingNumberGeneratorRepository extends JpaRepository<TrackingNumberGenerator, String>,
        TrackingNumberGeneratorExtRepository {
}
