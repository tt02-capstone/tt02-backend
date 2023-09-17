package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booking_id;

    private LocalDateTime start_datetime;

    private LocalDateTime end_datetime;

    private LocalDateTime last_update;

    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status;

    @Enumerated(EnumType.STRING)
    private BookingTypeEnum type;

    @OneToOne(mappedBy = "booking" , fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(fetch = FetchType.LAZY)
    private Deal deal;

    @OneToOne(fetch = FetchType.LAZY)
    private Telecom telecom;

    @OneToOne(fetch = FetchType.LAZY)
    private Tour tour;

    @OneToOne(fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tourist_id")
    private Tourist tourist_user;

    @ManyToOne
    @JoinColumn(name = "local_id")
    private Local local_user;
}
