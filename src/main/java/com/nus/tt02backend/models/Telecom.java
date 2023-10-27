package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.GBLimitEnum;
import com.nus.tt02backend.models.enums.NumberOfValidDaysEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.TelecomTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Telecom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long telecom_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean is_published;

    @Enumerated(EnumType.STRING)
    private TelecomTypeEnum type;

    @Enumerated(EnumType.STRING)
    private PriceTierEnum estimated_price_tier;

    @Column(nullable = false, length = 2)
    private Integer num_of_days_valid;

    @Enumerated(EnumType.STRING)
    private NumberOfValidDaysEnum plan_duration_category;

    @Column(nullable = false, length = 3)
    private Integer data_limit;

    @Enumerated(EnumType.STRING)
    private GBLimitEnum data_limit_category;

    private String image;

}
