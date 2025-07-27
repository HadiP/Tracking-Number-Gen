package com.tele.microservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_records")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tracking_number", unique = true)
    private String trackingNumber;

    @Column(name = "origin_country_id", nullable = false, length = 2)
    @NotBlank
    @Size(min = 2, max = 2)
    @Pattern(regexp = "^[A-Z]{2}$", message = "Must be ISO 3166-1 alpha-2 format")
    private String originCountryId;  // e.g., "MY"

    @Column(name = "destination_country_id", nullable = false, length = 2)
    @NotBlank
    @Size(min = 2, max = 2)
    @Pattern(regexp = "^[A-Z]{2}$", message = "Must be ISO 3166-1 alpha-2 format")
    private String destinationCountryId;  // e.g., "ID"

    @Column(nullable = false, precision = 6, scale = 3)
    @Positive
    @DecimalMin("0.001")
    private Double weight;  // in kilograms, up to 3 decimal places

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;  // RFC 3339 format

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;  // e.g., "de619854-b59b-425e-9db4-943979e1bd49"

    @Column(name = "customer_name", nullable = false)
    @NotBlank
    private String customerName;  // e.g., "RedBox Logistics"

    @Column(name = "customer_slug", nullable = false)
    @NotBlank
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Must be in kebab-case format")
    private String customerSlug;  // e.g., "redbox-logistics"

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
