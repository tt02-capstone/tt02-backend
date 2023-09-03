package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

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

    public Tour(LocalDateTime start_datetime, LocalDateTime end_datetime) {
        this.start_datetime = start_datetime;
        this.end_datetime = end_datetime;
    }
}