package com.nus.tt02backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DIYEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diy_event_id;

    @Column(nullable = false, length = 128)
    private String name;

    private LocalDateTime start_datetime;

    private LocalDateTime end_datetime;

    private String location;

    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY)
    private Telecom telecom;

    @ManyToOne(fetch = FetchType.LAZY)
    private TourType tourtype;

    @ManyToOne(fetch = FetchType.LAZY)
    private Accommodation accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Attraction attraction;

    @ManyToOne(fetch = FetchType.LAZY)
    private Restaurant restaurant;

    @OneToOne
    private Booking booking;
}
