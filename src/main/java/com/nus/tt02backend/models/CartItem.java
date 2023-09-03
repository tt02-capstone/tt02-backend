package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cart_item_id;
    private Integer quality;
    private String discountCode;
    private BigDecimal price;

    @OneToOne(fetch = FetchType.LAZY)
    private Deals deal;

    @OneToOne(fetch = FetchType.LAZY)
    private Telecom telecom;

    @OneToOne(fetch = FetchType.LAZY)
    private Tour tour;

    @OneToOne(fetch = FetchType.LAZY)
    private Room room;

    @OneToOne(fetch = FetchType.LAZY)
    private Attraction attraction;

    @OneToOne(fetch = FetchType.LAZY)
    private Restaurant restaurant;

}
