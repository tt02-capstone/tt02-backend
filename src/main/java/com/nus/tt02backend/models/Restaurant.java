package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.RestaurantEnum;
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
    @CollectionTable(name="image_list")
    private ArrayList<String> image_list = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private ArrayList<Dish> dish_list = new ArrayList<>();

    public Restaurant(Long restaurant_id, String name, String description, String address, String opening_hours, String contact_num, Boolean is_published, Integer suggested_duration, RestaurantEnum restaurant_type, GenericLocationEnum generic_location, PriceTierEnum estimated_price_tier, ArrayList<String> image_list) {
        this.restaurant_id = restaurant_id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.opening_hours = opening_hours;
        this.contact_num = contact_num;
        this.is_published = is_published;
        this.suggested_duration = suggested_duration;
        this.restaurant_type = restaurant_type;
        this.generic_location = generic_location;
        this.estimated_price_tier = estimated_price_tier;
        this.image_list = image_list;
    }
}
