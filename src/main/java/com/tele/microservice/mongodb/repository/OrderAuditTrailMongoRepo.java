package com.tele.microservice.mongodb.repository;

import com.tele.microservice.mongodb.entity.OrderAuditTrail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAuditTrailMongoRepo extends MongoRepository<OrderAuditTrail, String> {
}

