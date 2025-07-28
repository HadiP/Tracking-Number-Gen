package com.tele.microservice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.entity.TrackingNumberGenerator;
import com.tele.microservice.exception.InvalidDateException;
import com.tele.microservice.exception.NumberGeneratorException;
import com.tele.microservice.repository.TrackingNumberGeneratorRepository;

import com.tele.microservice.service.impl.TrackingNumberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TrackingNumberServiceImplTest {

    @Mock
    TrackingNumberGeneratorRepository repo;

    @InjectMocks
    TrackingNumberServiceImpl service;

    // A sample order request wrapper
    private final OrderRequestWrapper orderReq = new OrderRequestWrapper(UUID.randomUUID(),"MY",
                    "ID", 10d,
                    OffsetDateTime.parse("2025-07-25T10:15:30+07:00"),
                    UUID.randomUUID(), "Red Box Logistics", "redbox-logistics");

    @BeforeEach
    void setup() {
        // lower the refillThreshold to force refillBuffer in tests
        ReflectionTestUtils.setField(service, "refillThreshold", 5L);
    }

    @Test
    void nextTrackingNumber_refillsAndReturnsFormatted() throws Exception {
        // reserveBlock returns [start=1, end=6) → buffer: {1,2,3,4,5}
        when(repo.reserveBlock("MY")).thenReturn(Pair.of(1L, 6L));

        String tn = service.nextTrackingNumber(orderReq);

        // should consume '1', format as base-36 date + zero-padded seq
        assertThat(tn).startsWith("MY");
        assertThat(tn.length()).isEqualTo(14);

        // refillBuffer called exactly once
        verify(repo, times(1)).reserveBlock("MY");
    }

    @Test
    void nextTrackingNumber_whenReserveBlockReturnsNull_throws() {
        when(repo.reserveBlock("MY")).thenReturn(null);

        assertThatThrownBy(() -> service.nextTrackingNumber(orderReq))
                .isInstanceOf(NumberGeneratorException.class)
                .hasMessageContaining("Unable to find tracking number generator for MY");
    }

    @Test
    void nextTrackingNumber_onMaxSequence_triggersReset() throws Exception {
        // pre-load buffer so poll() == MAX_SEQUENCE
        // simulate refillBuffer
        when(repo.reserveBlock("MY")).thenReturn(Pair.of(TrackingNumberServiceImpl.MAX_SEQUENCE, TrackingNumberServiceImpl.MAX_SEQUENCE + 1));
        // simulate findById and resetSequence
        TrackingNumberGenerator gen = new TrackingNumberGenerator();
        gen.setOriginCode("MY");
        gen.setLastResetDateTime(orderReq.createdAt().minusDays(1));
        when(repo.findById("MY")).thenReturn(Optional.of(gen));
        when(repo.resetSequence("MY", orderReq.createdAt()))
                .thenReturn(Pair.of(1L, 6L));

        // first call will refill buffer, poll() → MAX_SEQUENCE → triggers reset
        String tn = service.nextTrackingNumber(orderReq);

        verify(repo).resetSequence("MY", orderReq.createdAt());
        assertThat(tn).startsWith("MY");
    }

    @Test
    void resetSequence_missingGenerator_throwsNumberGeneratorException() throws Exception {
        when(repo.reserveBlock("MY")).thenReturn(Pair.of(TrackingNumberServiceImpl.MAX_SEQUENCE, TrackingNumberServiceImpl.MAX_SEQUENCE + 1));
        when(repo.findById("MY")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.nextTrackingNumber(orderReq))
                .isInstanceOf(NumberGeneratorException.class)
                .hasMessageContaining("Unable to find tracking number generator for MY");
    }

    @Test
    void resetSequence_invalidDate_throwsInvalidDateException() throws Exception {
        // buffer → {MAX_SEQUENCE}
        when(repo.reserveBlock("MY")).thenReturn(Pair.of(TrackingNumberServiceImpl.MAX_SEQUENCE, TrackingNumberServiceImpl.MAX_SEQUENCE + 1));
        // findById returns entity with future lastResetDateTime
        TrackingNumberGenerator gen = new TrackingNumberGenerator();
        gen.setOriginCode("MY");
        gen.setLastResetDateTime(orderReq.createdAt().plusDays(1));
        when(repo.findById("MY")).thenReturn(Optional.of(gen));

        assertThatThrownBy(() -> service.nextTrackingNumber(orderReq))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining("Date order violation");
    }
}