package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TourType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tour_type_id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    private String image;

    @Column(nullable = false)
    private Integer recommended_pax;

    private String special_note;

    @Column(nullable = false)
    private Integer estimated_duration;

    @Column(nullable = false)
    private Boolean is_published;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Tour> tour_list;

    public TourType(BigDecimal price, String name,
                    String description, Integer recommended_pax,
                    Integer estimated_duration, Boolean is_published) {
        this.price = price;
        this.name = name;
        this.description = description;
        this.recommended_pax = recommended_pax;
        this.estimated_duration = estimated_duration;
        this.is_published = is_published;
    }
}