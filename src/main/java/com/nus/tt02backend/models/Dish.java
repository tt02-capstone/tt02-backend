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

    @Column(nullable = false, length = 400)
    private String description;
    private BigDecimal price;
    private Integer spice_level;
    private Boolean is_signature;
    private Boolean is_chef_recommendation;

    @ElementCollection
    @CollectionTable(name="image_list")
    private ArrayList<String> image_list = new ArrayList<String>();;


}
