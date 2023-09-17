package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.BookingTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booking_item_id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Date start_datetime;

    @Column(nullable = false)
    private Date end_datetime;

    @Column(nullable = false)
    private BookingTypeEnum type;

    @Column(nullable = false)
    private String activity_selection;

}