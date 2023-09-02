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

    public Long getSeasonal_activity_id() {
        return seasonal_activity_id;
    }

    public void setSeasonal_activity_id(Long seasonal_activity_id) {
        this.seasonal_activity_id = seasonal_activity_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStart_datetime() {
        return start_datetime;
    }

    public void setStart_datetime(LocalDateTime start_datetime) {
        this.start_datetime = start_datetime;
    }

    public LocalDateTime getEnd_datetime() {
        return end_datetime;
    }

    public void setEnd_datetime(LocalDateTime end_datetime) {
        this.end_datetime = end_datetime;
    }

    public Integer getSuggested_duration() {
        return suggested_duration;
    }

    public void setSuggested_duration(Integer suggested_duration) {
        this.suggested_duration = suggested_duration;
    }
}