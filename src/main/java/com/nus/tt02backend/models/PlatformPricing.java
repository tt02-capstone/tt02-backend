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

public class PlatformPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long platform_pricing_id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subscription_fee;

    @Column(nullable = false, precision = 3, scale = 3)
    private BigDecimal commission_percentage;

}
