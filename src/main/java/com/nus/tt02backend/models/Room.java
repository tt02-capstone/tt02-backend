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

    private String room_number;

    @Column(nullable = false, length = 400)
    private String amenities_description;

    private Integer num_of_pax;

    private BigDecimal price;

    private RoomTypeEnum room_type;
}
