package com.tele.microservice.dto;

import com.tele.microservice.exception.IllegalInputFormatException;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Order Request Wrapper used to request order.
 * @param originCountryId
 * @param destinationCountryId
 * @param weight
 * @param createdAt
 * @param customerId
 * @param customerName
 * @param customerSlug
 */
public record OrderRequestWrapper(
        UUID orderId,                 // UUID used as idempotency id
        String originCountryId,       // ISO 3166-1 alpha-2 (e.g., "MY")
        String destinationCountryId,  // ISO 3166-1 alpha-2 (e.g., "ID")
        double weight,                // in kilograms, up to 3 decimal places
        OffsetDateTime createdAt,     // RFC 3339 format
        UUID customerId,              // UUID
        String customerName,          // e.g., "RedBox Logistics"
        String customerSlug           // kebab-case e.g., "redbox-logistics"
) {
    // Validation patterns
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    public OrderRequestWrapper {
        // Validate country codes
        if (!COUNTRY_CODE_PATTERN.matcher(originCountryId).matches()) {
            throw new IllegalInputFormatException("Origin country must be ISO 3166-1 alpha-2 format");
        }
        if (!COUNTRY_CODE_PATTERN.matcher(destinationCountryId).matches()) {
            throw new IllegalInputFormatException("Destination country must be ISO 3166-1 alpha-2 format");
        }

        // Validate weight
        if (weight <= 0) {
            throw new IllegalInputFormatException("Weight must be positive");
        }

        // Round weight to 3 decimal places
        weight = Math.round(weight * 1000) / 1000.0;

        // Validate customer name
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalInputFormatException("Customer name cannot be blank");
        }

        // Validate customer slug
        if (customerSlug == null || !SLUG_PATTERN.matcher(customerSlug).matches()) {
            throw new IllegalInputFormatException("Customer slug must be in kebab-case format");
        }
    }
}