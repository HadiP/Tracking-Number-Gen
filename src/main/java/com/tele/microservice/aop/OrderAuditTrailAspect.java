package com.tele.microservice.aop;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.entity.OrderRecord;
import com.tele.microservice.mongodb.entity.AuditEventTypeEnum;
import com.tele.microservice.mongodb.entity.AuditStatusEnum;
import com.tele.microservice.mongodb.service.OrderAuditTrailService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@RequiredArgsConstructor
public class OrderAuditTrailAspect {

    private final OrderAuditTrailService auditTrailService;

    @AfterReturning(pointcut = """
            execution(* com.tele.microservice.service.OrderRecordService.saveOrder(
              com.tele.microservice.dto.OrderRequestWrapper, String
            )) && args(request, tracking)
            """, returning = "order")
    public void logOnSuccess(OrderRequestWrapper request, String tracking, OrderRecord order) {
        auditTrailService.logEvent(order.getId(),
                tracking,
                AuditStatusEnum.SUCCESS,
                order.getCustomerId(),
                order.getCustomerName(),
                AuditEventTypeEnum.CREATION
        );
    }

    @AfterThrowing(pointcut = """
            execution(* com.tele.microservice.service.OrderRecordService.saveOrder(
              com.tele.microservice.dto.OrderRequestWrapper, String
            )) && args(request, tracking)
            """, throwing = "ex")
    public void logOnError(OrderRequestWrapper request, String tracking, Throwable ex) {
        auditTrailService.logEvent(request.orderId(),
                tracking,
                AuditStatusEnum.ERROR,
                request.customerId(),
                request.customerName(),
                AuditEventTypeEnum.CREATION
        );
    }
}