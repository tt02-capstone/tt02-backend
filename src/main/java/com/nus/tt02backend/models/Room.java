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

    public Long getRoom_id() {
        return room_id;
    }

    public void setRoom_id(Long room_id) {
        this.room_id = room_id;
    }

    public String getRoom_number() {
        return room_number;
    }

    public void setRoom_number(String room_number) {
        this.room_number = room_number;
    }

    public String getAmenities_description() {
        return amenities_description;
    }

    public void setAmenities_description(String amenities_description) {
        this.amenities_description = amenities_description;
    }

    public Integer getNum_of_pax() {
        return num_of_pax;
    }

    public void setNum_of_pax(Integer num_of_pax) {
        this.num_of_pax = num_of_pax;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public RoomTypeEnum getRoom_type() {
        return room_type;
    }

    public void setRoom_type(RoomTypeEnum room_type) {
        this.room_type = room_type;
    }
}