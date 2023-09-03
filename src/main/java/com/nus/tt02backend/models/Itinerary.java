package com.nus.tt02backend.models;


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

public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itinerary_id;

    @Column(nullable = false)
    private Date start_date;

    @Column(nullable = false)
    private Date end_date;

    private Integer number_of_pax;

    @Column(nullable = false)
    private String remarks;

    @OneToMany(fetch = FetchType.LAZY)
    private List<DIYEvent> diy_event_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Booking> booking_list;

}
