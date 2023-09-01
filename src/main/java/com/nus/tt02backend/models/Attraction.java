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
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attraction_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;
    private String opening_hours;
    private String age_group;
    private String contact_num;

    @ElementCollection
    @CollectionTable(name="image")
    private ArrayList<String> image_list = new ArrayList<String>();;
    private Boolean is_published;
    private Integer suggested_duration;
//    private AttractionCategory attraction_category;
//    private GenericLocation generic_location;
//    private PriceTierEnum estimated_price_tier;
//    private RatingEnum avg_rating_tier;

}
