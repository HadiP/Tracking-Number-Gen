package com.tele.microservice.service.impl;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.repository.TrackingNumberGeneratorRepository;
import com.tele.microservice.service.TrackingNumberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.tele.microservice.util.TrackingNumberFormatting.formatWithBusinessLogic;

public class TrackingNumberServiceImpl implements TrackingNumberService {

    private final TrackingNumberGeneratorRepository repository;

    private final Queue<Long> buffer = new ConcurrentLinkedQueue<>();

    @Value("${tracking.number.refill.threshold:1000}")
    private int refillThreshold;


    public TrackingNumberServiceImpl(TrackingNumberGeneratorRepository repository) {
        this.repository = repository;
    }

    @Override
    public String nextTrackingNumber(OrderRequestWrapper orderRequest) {
        if (buffer.size() < refillThreshold) {
            refillBuffer();
        }
        long id = buffer.poll();
        return formatWithBusinessLogic(orderRequest.createdAt().toLocalDate(), orderRequest.originCountryId(), id);

    }

    @Transactional
    private synchronized void refillBuffer() {
        Pair<Long, Long> range = repository.reserveBlock("default");
        for (long i = range.getFirst(); i < range.getSecond(); i++) {
            buffer.add(i);
        }
    }

}
