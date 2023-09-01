package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accommodation_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    private String contact_num;

    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;

    @ElementCollection
    @CollectionTable(name="image_list")
    private ArrayList<String> image_list = new ArrayList<>();;
    private Boolean is_published;
//    private AccomodationTypeEnum type;
//    private GenericLocation generic_location;
//    private PriceTierEnum estimated_price_tier;

}
