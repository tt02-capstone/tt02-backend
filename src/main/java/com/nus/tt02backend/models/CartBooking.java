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
public class CartBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cart_booking_id;

    private LocalDateTime start_datetime;

    private LocalDateTime end_datetime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vendor vendor;

    @Enumerated(EnumType.STRING)
    private BookingTypeEnum type;

    private String activity_name;


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

    @ManyToOne
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToMany(fetch = FetchType.LAZY)
    private List<CartItem> cart_item_list;
}