package com.nus.tt02backend.models;

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
public class Telecom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long telecom_id;

    @Column(nullable = false, length = 128)
    private String telecom_name;

    @Column(nullable = false, unique = true, length = 400)
    private String description;

    private BigDecimal price;
    private Boolean is_published;
//    private TelecomTypeEnum type;
//    private PriceTierEnum estimated_price_tier;
//    private NumberOfValidDaysEnum num_of_days_valid;
//    private GBLimitEnum data_limit;

}
