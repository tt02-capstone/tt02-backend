package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurant_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @OneToOne
    private Address address;

    private String opening_hours;
    private String contact_num;

    @ElementCollection
    @CollectionTable(name="image_list")
    private ArrayList<String> image_list = new ArrayList<String>();;
    private Boolean is_published;
    private Integer suggested_duration;
//    private RestaurantEnum restaurant_type;
//    private GenericLocationEnum generic_location;
//    private PriceTierEnum estimated_price_tier;
//    private RatingEnum avg_rating_tier;
}
