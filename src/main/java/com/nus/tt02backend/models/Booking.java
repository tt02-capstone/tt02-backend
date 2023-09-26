package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
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

    private String activity_name;

    @Enumerated(EnumType.STRING)
    private UserTypeEnum booked_user;

    @OneToOne(mappedBy = "booking" , fetch = FetchType.LAZY)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @ManyToOne
    @JoinColumn(name = "telecom_id")
    private Telecom telecom;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "room_id")
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

    @OneToMany(fetch = FetchType.LAZY)
    private List<BookingItem> booking_item_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<QrCode> qr_code_list;
}
