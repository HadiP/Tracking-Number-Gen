package com.tele.microservice.mongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document(collection = "order_audit_trails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAuditTrail {

    public static final String STATUS = "status";
    public static final String EVENT_TYPE = "event_type";

    @Id
    private String id;

    private OffsetDateTime timestamp;

    private String trackingNumber;

    private AuditStatusEnum status;       // "success" or "error"

    private UUID customerId;

    private String customerName;

    private AuditEventTypeEnum eventType;    // "creation", "update", "delete"

    private UUID orderId;
}