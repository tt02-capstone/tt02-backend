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

    @OneToMany
    @JoinColumn(nullable = true)
    private List<Tour> tour_list = new ArrayList<>();

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

    public Long getTour_type_id() {
        return tour_type_id;
    }

    public void setTour_type_id(Long tour_type_id) {
        this.tour_type_id = tour_type_id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getRecommended_pax() {
        return recommended_pax;
    }

    public void setRecommended_pax(Integer recommended_pax) {
        this.recommended_pax = recommended_pax;
    }

    public String getSpecial_note() {
        return special_note;
    }

    public void setSpecial_note(String special_note) {
        this.special_note = special_note;
    }

    public Integer getEstimated_duration() {
        return estimated_duration;
    }

    public void setEstimated_duration(Integer estimated_duration) {
        this.estimated_duration = estimated_duration;
    }

    public Boolean getIs_published() {
        return is_published;
    }

    public void setIs_published(Boolean is_published) {
        this.is_published = is_published;
    }

    public List<Tour> getTour_list() {
        return tour_list;
    }

    public void setTour_list(List<Tour> tour_list) {
        this.tour_list = tour_list;
    }
}