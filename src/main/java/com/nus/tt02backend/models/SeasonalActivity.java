package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seasonal_activity_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(nullable = false)
    private LocalDateTime start_datetime;

    @Column(nullable = false)
    private LocalDateTime end_datetime;

    @Column(nullable = false)
    private Integer suggested_duration;

    public SeasonalActivity(String name, String description, LocalDateTime start_datetime,
                            LocalDateTime end_datetime, Integer suggested_duration) {
        this.name = name;
        this.description = description;
        this.start_datetime = start_datetime;
        this.end_datetime = end_datetime;
        this.suggested_duration = suggested_duration;
    }
}