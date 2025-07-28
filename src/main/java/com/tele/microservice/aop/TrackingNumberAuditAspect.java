package com.tele.microservice.aop;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.mongodb.entity.AuditEventTypeEnum;
import com.tele.microservice.mongodb.entity.AuditStatusEnum;
import com.tele.microservice.mongodb.entity.OrderAuditTrail;
import com.tele.microservice.mongodb.service.OrderAuditTrailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect used to track statuses of tracking number generation process.
 */
@Aspect
@Component
public class TrackingNumberAuditAspect {

    private final OrderAuditTrailService auditTrailService;
    private final Counter generationSuccessCounter;
    private final Counter generationErrorCounter;

    /**
     * Since I don't want to add extra logic on tracking number service add the metric counter in the aspect class.
     * @param auditTrailService
     * @param meterRegistry
     */
    public TrackingNumberAuditAspect(
        OrderAuditTrailService auditTrailService,
        MeterRegistry meterRegistry
    ) {
        this.auditTrailService = auditTrailService;

        this.generationSuccessCounter = Counter.builder("audit.generation.events")
            .tag(OrderAuditTrail.STATUS, AuditStatusEnum.SUCCESS.toString())
            .description("Number of successful tracking-number generations")
            .register(meterRegistry);

        this.generationErrorCounter = Counter.builder("audit.generation.events")
            .tag(OrderAuditTrail.STATUS, AuditStatusEnum.ERROR.toString())
            .description("Number of failed tracking-number generations")
            .register(meterRegistry);
    }

    /**
     * On successful generation, increment success counter & log audit.
     */
    @AfterReturning(
            pointcut = "execution(* com.tele.microservice.service.TrackingNumberService.nextTrackingNumber(..)) && args(orderRequest)",
            returning = "trackingNumber"
    )
    public void logGenerationSuccess(OrderRequestWrapper orderRequest, String trackingNumber) {
        generationSuccessCounter.increment();

        auditTrailService.logEvent(
                orderRequest.orderId(),
                trackingNumber,
                AuditStatusEnum.SUCCESS,
                orderRequest.customerId(),
                orderRequest.customerName(),
                AuditEventTypeEnum.GENERATION
        );
    }

    /**
     * On exception (NumberGeneratorException or InvalidDateException), log an ERROR.
     */
    @AfterThrowing(
            pointcut = "execution(* com.tele.microservice.service.TrackingNumberService.nextTrackingNumber(..)) && args(orderRequest)",
            throwing = "ex"
    )
    public void logGenerationError(OrderRequestWrapper orderRequest, Throwable ex) {
        generationErrorCounter.increment();

        auditTrailService.logEvent(
                orderRequest.orderId(),
                "n/a",
                AuditStatusEnum.ERROR,
                orderRequest.customerId(),
                orderRequest.customerName(),
                AuditEventTypeEnum.GENERATION
        );
    }
}