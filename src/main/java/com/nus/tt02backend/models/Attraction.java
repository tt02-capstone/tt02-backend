package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import com.nus.tt02backend.models.enums.AttractionCategoryEnum;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;

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

    @Column(nullable = false)
    private String opening_hours;

    @Column(nullable = false)
    private String age_group;

    @Column(nullable = false)
    private String contact_num;

    @ElementCollection
    @CollectionTable(name="image")
    private List<String> image_list = new ArrayList<>();

    @Column(nullable = false)
    private Boolean is_published;

    @Column(nullable = false)
    private Integer suggested_duration;

    @Column(nullable = false)
    private Double avg_rating_tier = 0.0;

    @Enumerated(EnumType.STRING)
    private AttractionCategoryEnum attraction_category;

    @Enumerated(EnumType.STRING)
    private GenericLocationEnum generic_location;

    @Enumerated(EnumType.STRING)
    private PriceTierEnum estimated_price_tier;

    @OneToMany
    @JoinColumn(nullable = true)
    private List<SeasonalActivity> seasonal_activity_list = new ArrayList<>();

    @OneToMany
    @JoinColumn(nullable = false)
    private List<Price> price_list = new ArrayList<>();

    @OneToMany
    @JoinColumn(nullable = true)
    private List<Review> review_list = new ArrayList<>();

    @OneToMany
    @JoinColumn(nullable = true)
    private List<TourType> tour_type_list = new ArrayList<>();

    @OneToOne
    @JoinColumn(nullable = false)
    private Address address;

    public Attraction(String name, String description, String opening_hours,
                      String age_group, String contact_num, Boolean is_published,
                      Integer suggested_duration, AttractionCategoryEnum attraction_category,
                      GenericLocationEnum generic_location, PriceTierEnum estimated_price_tier,
                      List<Price> price_list, Address address) {
        this.name = name;
        this.description = description;
        this.opening_hours = opening_hours;
        this.age_group = age_group;
        this.contact_num = contact_num;
        this.is_published = is_published;
        this.suggested_duration = suggested_duration;
        this.attraction_category = attraction_category;
        this.generic_location = generic_location;
        this.estimated_price_tier = estimated_price_tier;
        this.price_list = price_list;
        this.address = address;
    }
}