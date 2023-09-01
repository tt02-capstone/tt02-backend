package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

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

    @ManyToOne
    @JoinColumn(name="itinerary_id")
    private Itinerary itinerary;
    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;
    private Date last_update;

    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status;

    @Enumerated(EnumType.STRING)
    private BookingTypeEnum type;

    @OneToOne(mappedBy = "booking")
    private Payment payment;
}
