package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private LocalDate start_date;

    @Column(nullable = false)
    private LocalDate end_date;

    @Column(nullable = false)
    private Integer suggested_duration;
}