package com.tele.microservice.mongodb.service;

import com.tele.microservice.mongodb.entity.AuditEventTypeEnum;
import com.tele.microservice.mongodb.entity.AuditStatusEnum;

import java.util.UUID;

public interface OrderAuditTrailService {

    void logEvent(
            UUID orderId,
            String trackingNumber,
            AuditStatusEnum status,
            UUID customerId,
            String customerName,
            AuditEventTypeEnum eventType
    );

}
