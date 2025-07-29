package com.tele.microservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record OrderResponseWrapper(
        @JsonProperty("tracking_number")
        String trackingNumber,

        @JsonProperty("created_at")
        OffsetDateTime createdAt
) { }
