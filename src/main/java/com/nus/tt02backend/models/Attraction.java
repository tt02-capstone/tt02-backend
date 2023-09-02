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

    private String opening_hours;

    private String age_group;

    private String contact_num;

    @ElementCollection
    @CollectionTable(name="image")
    private List<String> image_list = new ArrayList<String>();;

    private Boolean is_published;

    private Integer suggested_duration;

    private Double avg_rating_tier;

    private AttractionCategoryEnum attraction_category;

    private GenericLocationEnum generic_location;

    private PriceTierEnum estimated_price_tier;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
