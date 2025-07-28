package com.tele.microservice.aop;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.entity.OrderRecord;
import com.tele.microservice.mongodb.entity.AuditEventTypeEnum;
import com.tele.microservice.mongodb.entity.AuditStatusEnum;
import com.tele.microservice.mongodb.service.OrderAuditTrailService;
import com.tele.microservice.repository.OrderRecordRepository;
import com.tele.microservice.service.OrderRecordService;
import com.tele.microservice.service.impl.OrderRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrderAuditTrailAspectTest.TestConfig.class)
class OrderAuditTrailAspectTest {

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean OrderAuditTrailService auditTrailService() {
            return mock(OrderAuditTrailService.class);
        }

        @Bean OrderRecordRepository orderRecordRepository() {
            return mock(OrderRecordRepository.class);
        }

        @Bean OrderRecordService orderRecordService(OrderRecordRepository repo) {
            return new OrderRecordServiceImpl(repo);
        }

        @Bean OrderAuditTrailAspect orderAuditTrailAspect(
            OrderAuditTrailService auditTrailService
        ) {
            return new OrderAuditTrailAspect(auditTrailService);
        }
    }

    @Autowired
    OrderRecordService orderRecordService;
    @Autowired
    OrderRecordRepository repository;
    @Autowired
    OrderAuditTrailService auditTrailService;

    private final OrderRequestWrapper orderReq = new OrderRequestWrapper(
        UUID.randomUUID(),
        "MY",
        "ID",
        10d,
            OffsetDateTime.now(),
        UUID.randomUUID(),
        "Red Box Logistics",
        "redbox-logistics"
    );

    @Test
    void whenSaveOrderSucceeds_thenAuditLogEventWithSuccess() {
        // stub any OrderRecord to be returned back
        when(repository.save(any(OrderRecord.class)))
            .thenReturn(OrderRecord.builder()
                    .id(orderReq.orderId())
                    .trackingNumber("TRACK123")
                    .customerId(orderReq.customerId())
                    .customerName(orderReq.customerName())
                    .customerSlug(orderReq.customerSlug())
                    .createdAt(orderReq.createdAt())
                    .build()
);

        // call the method under test
        OrderRecord returned = orderRecordService.saveOrder(orderReq, "TRACK123");

        // we should get back a non-null OrderRecord whose fields match the wrapper
        assertNotNull(returned);
        assertEquals(orderReq.orderId(), returned.getId());
        assertEquals("TRACK123", returned.getTrackingNumber());
        assertEquals(orderReq.customerId(), returned.getCustomerId());
        assertEquals(orderReq.customerName(), returned.getCustomerName());

        // verify that the AOP advice did fire
        verify(auditTrailService, times(1)).logEvent(
            eq(returned.getId()),
                        eq("TRACK123"),
                        eq(AuditStatusEnum.SUCCESS),
            eq(returned.getCustomerId()),
            eq(returned.getCustomerName()),
                        eq(AuditEventTypeEnum.CREATION)
                );
    }

    @Test
    void whenSaveOrderThrows_thenAuditLogEventWithError() {
        // stub any OrderRecord save to throw
        when(repository.save(any(OrderRecord.class)))
                .thenThrow(new RuntimeException("DB down"));

        // exception is expected, but we still want to verify AOP
        assertThrows(
            RuntimeException.class,
            () -> orderRecordService.saveOrder(orderReq, "TRACK123")
        );

        verify(auditTrailService, times(1)).logEvent(
            eq(orderReq.orderId()),
                        eq("TRACK123"),
                        eq(AuditStatusEnum.ERROR),
            eq(orderReq.customerId()),
            eq(orderReq.customerName()),
                        eq(AuditEventTypeEnum.CREATION)
                );
    }
}