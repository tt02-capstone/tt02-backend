package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.nus.tt02backend.models.enums.RoomTypeEnum;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long room_id;

//    @Column(nullable = false)
//    private String room_number;

    @Column(nullable = false, length = 400)
    private String amenities_description;

    @Column(nullable = false)
    private Integer num_of_pax;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private RoomTypeEnum room_type;

    private Integer quantity;

    private String room_image;
}