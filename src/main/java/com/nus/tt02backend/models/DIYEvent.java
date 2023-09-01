package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

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

}
