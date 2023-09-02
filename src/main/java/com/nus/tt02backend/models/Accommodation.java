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

    public Boolean is_published;

    private LocalDateTime check_in_time;

    private LocalDateTime check_out_time;

    private AccommodationTypeEnum type;

    private GenericLocationEnum generic_location;

    private PriceTierEnum estimated_price_tier;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
