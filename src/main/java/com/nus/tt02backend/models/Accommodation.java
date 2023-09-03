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

    @Column(nullable = false)
    private String address;

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

    public Accommodation(String name, String description, String contact_num,
                         Boolean is_published, LocalDateTime check_in_time,
                         LocalDateTime check_out_time, AccommodationTypeEnum type,
                         GenericLocationEnum generic_location, PriceTierEnum estimated_price_tier,
                         String address) {
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
}