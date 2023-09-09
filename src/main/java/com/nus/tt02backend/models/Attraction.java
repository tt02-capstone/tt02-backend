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
    private String address;

    @Column(nullable = false)
    private String opening_hours;

    @Column(nullable = false)
    private String age_group;

    @Column(nullable = false)
    private String contact_num;

    @ElementCollection
    @CollectionTable(name="attraction_image_list")
    private List<String> attraction_image_list;

    @Column(nullable = false)
    private Boolean is_published;

    @Column(nullable = false)
    private Integer suggested_duration;

    @Column(nullable = false)
    private Double avg_rating_tier;

    @Enumerated(EnumType.STRING)
    private AttractionCategoryEnum attraction_category;

    @Enumerated(EnumType.STRING)
    private GenericLocationEnum generic_location;

    @Enumerated(EnumType.STRING)
    private PriceTierEnum estimated_price_tier;

    @OneToMany(fetch = FetchType.LAZY)
    private List<SeasonalActivity> seasonal_activity_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Price> price_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Review> review_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<TourType> tour_type_list;

}