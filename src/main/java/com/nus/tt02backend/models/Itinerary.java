package com.nus.tt02backend.models;


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
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itinerary_id;

    private Date start_date;

    private Date end_date;
    private Integer number_of_pax;
    private String remarks;

    @OneToMany(mappedBy = "itinerary")
    private ArrayList<Booking> booking_list= new ArrayList<>();
}
