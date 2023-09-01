package com.nus.tt02backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private Long pricing_platform_id;

    private BigDecimal subscription_fee;
    private BigDecimal commission_percentage;

//    private TicketEnum ticket_type;

}
