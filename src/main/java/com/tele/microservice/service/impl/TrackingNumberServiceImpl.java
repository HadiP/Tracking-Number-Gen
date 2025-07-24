package com.tele.microservice.service.impl;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.entity.TrackingNumberGenerator;
import com.tele.microservice.exception.InvalidDateException;
import com.tele.microservice.exception.NumberGeneratorException;
import com.tele.microservice.repository.TrackingNumberGeneratorRepository;
import com.tele.microservice.service.TrackingNumberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.tele.microservice.util.TrackingNumberFormatting.formatWithBusinessLogic;

@Service
public class TrackingNumberServiceImpl implements TrackingNumberService {

    private final TrackingNumberGeneratorRepository repository;

    private final Queue<Long> buffer = new ConcurrentLinkedQueue<>();

    /**
     * To avoid sequence generate number that exceed 7 digits when converted to base 36
     */
    public static final long MAX_SEQUENCE = 10000000;

    @Value("${tracking.number.refill.threshold:100}")
    private long refillThreshold;


    public TrackingNumberServiceImpl(TrackingNumberGeneratorRepository repository) {
        this.repository = repository;
    }

    /**
     * Get next tracking sequence number and format it into {@link com.tele.microservice.util.TrackingNumberFormatting}
     *
     * @param orderRequest
     * @return
     * @throws Exception
     */
    @Override
    public String nextTrackingNumber(OrderRequestWrapper orderRequest) throws Exception {
        if (buffer.size() < refillThreshold) {
            refillBuffer(orderRequest.originCountryId());
        }
        long seq = buffer.poll();
        if (seq == MAX_SEQUENCE) {
            resetSequence(orderRequest.originCountryId(), orderRequest.createdAt());
        }
        return formatWithBusinessLogic(orderRequest.createdAt().toLocalDate(), orderRequest.originCountryId(), seq);

    }

    /**
     * Refilling buffer to start generating tracking number
     *
     * @param origin
     */
    @Transactional
    private synchronized void refillBuffer(String origin) throws NumberGeneratorException {
        Pair<Long, Long> range = repository.reserveBlock(origin);
        if (range == null) {
            throw new NumberGeneratorException("Unable to find tracking number generator for " + origin);
        }
        for (long i = range.getFirst(); i < range.getSecond(); i++) {
            buffer.add(i);
        }
    }

    /**
     * Restart sequence and start over from 1 when generated sequence reach threshold of MAX_SEQUENCE
     *
     * @param origin
     * @param timestamp
     */
    @Transactional
    private synchronized void resetSequence(String origin, OffsetDateTime timestamp) throws NumberGeneratorException, InvalidDateException {
        Optional<TrackingNumberGenerator> numberGenerator = repository.findById(origin);
        if (numberGenerator.isEmpty()) {
            throw new NumberGeneratorException("Unable to find tracking number generator for " + origin);
        }
        LocalDate lastResetDate = numberGenerator.get().getLastResetDateTime().toLocalDate();
        LocalDate creationDate = timestamp.toLocalDate();
        if (lastResetDate.isAfter(creationDate)) {
            throw new InvalidDateException(String.format("Date order violation: %s (last reset date) cannot be after %s (creation date)",
                    lastResetDate, creationDate));
        }
        Pair<Long, Long> range = repository.resetSequence(origin, timestamp);
        for (long i = range.getFirst(); i < range.getSecond(); i++) {
            buffer.add(i);
        }
    }

}
