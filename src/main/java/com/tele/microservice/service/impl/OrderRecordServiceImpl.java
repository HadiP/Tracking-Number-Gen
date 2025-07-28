package com.tele.microservice.service.impl;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.entity.OrderRecord;
import com.tele.microservice.repository.OrderRecordRepository;
import com.tele.microservice.service.OrderRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderRecordServiceImpl implements OrderRecordService {

    private final OrderRecordRepository repository;

    @Transactional
    @Override
    public OrderRecord saveOrder(@Valid OrderRequestWrapper orderRequest, @NotNull String trackingNumber) {
        OrderRecord order = OrderRecord.builder()
                .trackingNumber(trackingNumber)
                .customerId(orderRequest.customerId())
                .customerName(orderRequest.customerName())
                .customerSlug(orderRequest.customerSlug())
                .originCountryId(orderRequest.originCountryId())
                .destinationCountryId(orderRequest.destinationCountryId())
                .createdAt(orderRequest.createdAt())
                .build();
        return repository.save(order);
    }
}
