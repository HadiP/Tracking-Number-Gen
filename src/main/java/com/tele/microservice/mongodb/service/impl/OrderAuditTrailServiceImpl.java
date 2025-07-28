package com.tele.microservice.mongodb.service.impl;

import com.tele.microservice.mongodb.entity.AuditEventTypeEnum;
import com.tele.microservice.mongodb.entity.AuditStatusEnum;
import com.tele.microservice.mongodb.entity.OrderAuditTrail;
import com.tele.microservice.mongodb.repository.OrderAuditTrailMongoRepo;
import com.tele.microservice.mongodb.service.OrderAuditTrailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Order audit trail service with custom metric counter for successful/failed event.
 */
@Service
public class OrderAuditTrailServiceImpl implements OrderAuditTrailService {

    private final OrderAuditTrailMongoRepo repository;
    private final Counter successCounter;
    private final Counter errorCounter;

    public OrderAuditTrailServiceImpl(OrderAuditTrailMongoRepo repository, MeterRegistry meterRegistry){
        this.repository = repository;
        this.successCounter = Counter.builder("order.audit.events")
                .tag(OrderAuditTrail.STATUS, AuditStatusEnum.SUCCESS.toString())
                .description("Number of successful audit events")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("order.audit.events")
                .tag(OrderAuditTrail.STATUS, AuditStatusEnum.SUCCESS.toString())
                .description("Number of failed audit events")
                .register(meterRegistry);
    }

    @Async
    public void logEvent(UUID orderId, String trackingNumber, AuditStatusEnum status,
                         UUID customerId, String customerName, AuditEventTypeEnum eventType) {
        if (AuditStatusEnum.SUCCESS.equals(status)) {
            successCounter.increment();
        } else {
            errorCounter.increment();
        }

        OrderAuditTrail trail = OrderAuditTrail.builder()
                .timestamp(OffsetDateTime.now())
                .trackingNumber(trackingNumber)
                .status(status)
                .customerId(customerId)
                .customerName(customerName)
                .eventType(eventType)
                .orderId(orderId)
                .build();

        repository.save(trail);
    }
}