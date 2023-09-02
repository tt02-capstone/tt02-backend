package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.models.enums.VendorEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendor_id;

    @Column(nullable = false, length = 128)
    private String business_name;

    @Column(nullable = false)
    private String poc_name;

    @Column(nullable = false)
    private String poc_position;

    @Column(nullable = false)
    private String country_code;

    @Column(nullable = false)
    private String poc_mobile_num;

    @Column(nullable = false)
    private BigDecimal wallet_balance;

    @Enumerated(EnumType.STRING)
    private ApplicationStatusEnum application_status;

    @Enumerated(EnumType.STRING)
    private VendorEnum vendor_type;

    @Column(nullable = false)
    private String service_description;

    @ManyToOne
    private Subscription subscription;

}
