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
    private Long activity_id;
    private String name;

    @Column(nullable = false, length = 400)
    private String description;
    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;
    private Integer suggested_duration;
}
