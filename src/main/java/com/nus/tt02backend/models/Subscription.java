package com.nus.tt02backend.models;

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

//    private SubscriptionPlanEnum subscription_plan;
    private Date subscription_expiry_date;

    @OneToMany(mappedBy = "subscription")
    private ArrayList<Vendor> vendor_list = new ArrayList<>();

}
