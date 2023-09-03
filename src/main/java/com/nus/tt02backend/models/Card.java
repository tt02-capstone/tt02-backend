package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long card_id;

    @Column(nullable = false, length = 16)
    private String card_num;

    private String hidden_card_num;

    @Column(nullable = false, length = 2)
    private String expiry_month;

    @Column(nullable = false, length = 2)
    private String expiry_year;

    @Column(nullable = true)
    private String billing_address;
}
