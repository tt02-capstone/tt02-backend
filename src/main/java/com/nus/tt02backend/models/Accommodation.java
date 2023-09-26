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
    @CollectionTable(name="accommodation_image_list")
    private List<String> accommodation_image_list;

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

    @OneToMany(fetch = FetchType.LAZY)
    private List<Room> room_list;

}