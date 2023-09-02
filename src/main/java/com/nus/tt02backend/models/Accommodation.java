package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import com.nus.tt02backend.models.enums.AccommodationTypeEnum;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accommodation_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    private String contact_num;

    @ElementCollection
    @CollectionTable(name="image_list")
    private List<String> image_list = new ArrayList<>();;

    @Column(nullable = false)
    public Boolean is_published;

    @Column(nullable = false)
    private LocalDateTime check_in_time;

    @Column(nullable = false)
    private LocalDateTime check_out_time;

    @Enumerated(EnumType.STRING)
    private AccommodationTypeEnum type;

    @Enumerated(EnumType.STRING)
    private GenericLocationEnum generic_location;

    @Enumerated(EnumType.STRING)
    private PriceTierEnum estimated_price_tier;

    @OneToMany
    @JoinColumn(nullable = true)
    private List<Room> room_list = new ArrayList<>();

    @OneToOne
    @JoinColumn(nullable = false)
    private Address address;

    public Accommodation(String name, String description, String contact_num,
                         Boolean is_published, LocalDateTime check_in_time,
                         LocalDateTime check_out_time, AccommodationTypeEnum type,
                         GenericLocationEnum generic_location, PriceTierEnum estimated_price_tier,
                         Address address) {
        this.name = name;
        this.description = description;
        this.contact_num = contact_num;
        this.is_published = is_published;
        this.check_in_time = check_in_time;
        this.check_out_time = check_out_time;
        this.type = type;
        this.generic_location = generic_location;
        this.estimated_price_tier = estimated_price_tier;
        this.address = address;
    }

    public Long getAccommodation_id() {
        return accommodation_id;
    }

    public void setAccommodation_id(Long accommodation_id) {
        this.accommodation_id = accommodation_id;
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

    public LocalDateTime getCheck_in_time() {
        return check_in_time;
    }

    public void setCheck_in_time(LocalDateTime check_in_time) {
        this.check_in_time = check_in_time;
    }

    public LocalDateTime getCheck_out_time() {
        return check_out_time;
    }

    public void setCheck_out_time(LocalDateTime check_out_time) {
        this.check_out_time = check_out_time;
    }

    public AccommodationTypeEnum getType() {
        return type;
    }

    public void setType(AccommodationTypeEnum type) {
        this.type = type;
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

    public List<Room> getRoom_list() {
        return room_list;
    }

    public void setRoom_list(List<Room> room_list) {
        this.room_list = room_list;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}