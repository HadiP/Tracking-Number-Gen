package com.tele.microservice.aop;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.exception.NumberGeneratorException;
import com.tele.microservice.mongodb.entity.AuditEventTypeEnum;
import com.tele.microservice.mongodb.entity.AuditStatusEnum;
import com.tele.microservice.mongodb.service.OrderAuditTrailService;
import com.tele.microservice.repository.TrackingNumberGeneratorRepository;
import com.tele.microservice.service.TrackingNumberService;
import com.tele.microservice.service.impl.TrackingNumberServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TrackingNumberAuditAspectTest.TestConfig.class)
class TrackingNumberAuditAspectTest {

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        TrackingNumberGeneratorRepository repository() {
            return mock(TrackingNumberGeneratorRepository.class);
        }

        @Bean
        TrackingNumberService service(TrackingNumberGeneratorRepository repository){
            return new TrackingNumberServiceImpl(repository);
        }

        @Bean
        OrderAuditTrailService auditTrailService() {
            return mock(OrderAuditTrailService.class);
        }

        @Bean
        MeterRegistry meterRegistry(){
            return new SimpleMeterRegistry();
        }

        @Bean
        TrackingNumberAuditAspect aspect(OrderAuditTrailService orderAuditTrailService, MeterRegistry meterRegistry){
            return new TrackingNumberAuditAspect(orderAuditTrailService, meterRegistry);
        }

    }

    @Autowired
    private TrackingNumberGeneratorRepository repository;

    @Autowired
    private TrackingNumberService service;

    @Autowired
    private OrderAuditTrailService auditTrailService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private TrackingNumberAuditAspect aspect;

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
    void whenNextTrackingNumberSucceeds_thenSuccessAdviceFires() throws Exception {
        // Arrange: stub repository.reserveBlock(...)
        when(repository.reserveBlock("MY"))
                .thenReturn(Pair.of(1L, 4L)); // will fill buffer with [1,2,3]

        // Act
        String tn = service.nextTrackingNumber(orderReq);

        // Assert counter
        Counter successCounter = meterRegistry
                .find("audit.generation.events")
                .tag("status", AuditStatusEnum.SUCCESS.name())
                .counter();
        assertThat(successCounter).isNotNull();
        assertThat(successCounter.count()).isEqualTo(1.0);

        // Assert audit call
        verify(auditTrailService)
                .logEvent(
                        orderReq.orderId(),
                        tn,
                        AuditStatusEnum.SUCCESS,
                        orderReq.customerId(),
                        orderReq.customerName(),
                        AuditEventTypeEnum.GENERATION
                );
    }

    @Test
    void whenReserveBlockReturnsNull_thenErrorAdviceFires() {
        // Arrange: cause refillBuffer to throw NumberGeneratorException
        when(repository.reserveBlock("MY")).thenReturn(null);

        // Act & Assert exception from service
        assertThrows(NumberGeneratorException.class, () -> service.nextTrackingNumber(orderReq));

        // Assert error counter
        Counter errorCounter = meterRegistry
                .find("audit.generation.events")
                .tag("status", AuditStatusEnum.ERROR.name())
                .counter();
        assertThat(errorCounter).isNotNull();
        assertThat(errorCounter.count()).isEqualTo(1.0);

        // Verify audit call with "n/a"
        verify(auditTrailService)
                .logEvent(
                        orderReq.orderId(),
                        "n/a",
                        AuditStatusEnum.ERROR,
                        orderReq.customerId(),
                        orderReq.customerName(),
                        AuditEventTypeEnum.GENERATION
                );
    }

}
