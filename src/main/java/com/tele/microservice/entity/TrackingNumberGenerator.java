package com.tele.microservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_id_generator")
@Getter
@Setter
@NoArgsConstructor
public class TrackingNumberGenerator {

    @Id
    @Column(name = "origin_code", length = 2)
    private String originCode;

    @Column(name = "next_value")
    private Long nextValue;
    @Column(name = "last_reset_timestamp")
    private OffsetDateTime lastResetDateTime;
    @Column(name = "block_size")
    private Integer blockSize;

}
