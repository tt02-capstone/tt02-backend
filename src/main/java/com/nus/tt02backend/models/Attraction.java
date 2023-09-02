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

    public Long getAttraction_id() {
        return attraction_id;
    }

    public void setAttraction_id(Long attraction_id) {
        this.attraction_id = attraction_id;
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

    public String getOpening_hours() {
        return opening_hours;
    }

    public void setOpening_hours(String opening_hours) {
        this.opening_hours = opening_hours;
    }

    public String getAge_group() {
        return age_group;
    }

    public void setAge_group(String age_group) {
        this.age_group = age_group;
    }

    public String getContact_num() {
        return contact_num;
    }

    public void setContact_num(String contact_num) {
        this.contact_num = contact_num;
    }

    public List<String> getImage_list() {
        return image_list;
    }

    public void setImage_list(List<String> image_list) {
        this.image_list = image_list;
    }

    public Boolean getIs_published() {
        return is_published;
    }

    public void setIs_published(Boolean is_published) {
        this.is_published = is_published;
    }

    public Integer getSuggested_duration() {
        return suggested_duration;
    }

    public void setSuggested_duration(Integer suggested_duration) {
        this.suggested_duration = suggested_duration;
    }

    public Double getAvg_rating_tier() {
        return avg_rating_tier;
    }

    public void setAvg_rating_tier(Double avg_rating_tier) {
        this.avg_rating_tier = avg_rating_tier;
    }

    public AttractionCategoryEnum getAttraction_category() {
        return attraction_category;
    }

    public void setAttraction_category(AttractionCategoryEnum attraction_category) {
        this.attraction_category = attraction_category;
    }

    public GenericLocationEnum getGeneric_location() {
        return generic_location;
    }

    public void setGeneric_location(GenericLocationEnum generic_location) {
        this.generic_location = generic_location;
    }

    public PriceTierEnum getEstimated_price_tier() {
        return estimated_price_tier;
    }

    public void setEstimated_price_tier(PriceTierEnum estimated_price_tier) {
        this.estimated_price_tier = estimated_price_tier;
    }

    public List<SeasonalActivity> getSeasonal_activity_list() {
        return seasonal_activity_list;
    }

    public void setSeasonal_activity_list(List<SeasonalActivity> seasonal_activity_list) {
        this.seasonal_activity_list = seasonal_activity_list;
    }

    public List<Price> getPrice_list() {
        return price_list;
    }

    public void setPrice_list(List<Price> price_list) {
        this.price_list = price_list;
    }

    public List<Review> getReview_list() {
        return review_list;
    }

    public void setReview_list(List<Review> review_list) {
        this.review_list = review_list;
    }

    public List<TourType> getTour_type_list() {
        return tour_type_list;
    }

    public void setTour_type_list(List<TourType> tour_type_list) {
        this.tour_type_list = tour_type_list;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}