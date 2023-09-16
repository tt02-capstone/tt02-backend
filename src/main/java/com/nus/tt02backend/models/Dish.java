package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dish_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer spice_level;

    @Column(nullable = false)
    private Boolean is_signature;

    @Column(nullable = false)
    private Boolean is_chef_recommendation;

    @ElementCollection
    @CollectionTable(name="dish_image_list")
    private ArrayList<String> dish_image_list;


}
