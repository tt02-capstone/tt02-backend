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

    @Column(nullable = false, length = 128)
    private String address_line_1;

    @Column(length = 128)
    private String address_line_2;

    @Column(nullable = false, length = 6)
    private String postal_code;

    @Column(nullable = false, length = 85)
    private String city;

    @Column(nullable = false, length = 56)
    private String country;

    @OneToOne
    private Restaurant restaurant;
}
