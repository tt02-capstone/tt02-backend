package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String room_number;

    @Column(nullable = false, length = 400)
    private String amenities_description;

    @Column(nullable = false)
    private Integer num_of_pax;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private RoomTypeEnum room_type;

    public Room(String room_number, String amenities_description,
                Integer num_of_pax, BigDecimal price, RoomTypeEnum room_type) {
        this.room_number = room_number;
        this.amenities_description = amenities_description;
        this.num_of_pax = num_of_pax;
        this.price = price;
        this.room_type = room_type;
    }
}