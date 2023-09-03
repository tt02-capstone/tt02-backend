package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.SubscriptionPlanEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscription_id;

    private Date subscription_expiry_date;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlanEnum subscription_plan;

}
