package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.RestaurantEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String opening_hours;

    @Column(nullable = false, length = 8)
    private String contact_num;

    @Column(nullable = false)
    private Boolean is_published = false;

    @Column(nullable = false)
    private Integer suggested_duration;

    @Enumerated(EnumType.STRING)
    private RestaurantEnum restaurant_type;

    @Enumerated(EnumType.STRING)
    private GenericLocationEnum generic_location;

    @Enumerated(EnumType.STRING)
    private PriceTierEnum estimated_price_tier;

    @ElementCollection
    @CollectionTable(name="restaurant_image_list")
    private List<String> restaurant_image_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Dish> dish_list;
}
