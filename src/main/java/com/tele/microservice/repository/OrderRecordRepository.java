package com.tele.microservice.repository;

import com.tele.microservice.entity.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecord, UUID> {
}
