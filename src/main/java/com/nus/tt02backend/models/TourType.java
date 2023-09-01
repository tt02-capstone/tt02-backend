package com.nus.tt02backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
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
    private BigDecimal price;
    private String description;
    private String image;
    private Integer recommended_pax;

    private String special_note;
    private String estimated_duration;
    private Boolean is_published;
    private Date published_date;
}
