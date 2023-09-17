package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tour_id;

    @Column(nullable = false)
    private LocalDateTime start_datetime;

    @Column(nullable = false)
    private LocalDateTime end_datetime;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
}