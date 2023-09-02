package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long address_id;

    @Column(nullable = false)
    private String address_line_1;

    @Column(nullable = false)
    private String address_line_2;

    @Column(nullable = false)
    private String postal_code;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @OneToOne
    private Restaurant restaurant;
}
