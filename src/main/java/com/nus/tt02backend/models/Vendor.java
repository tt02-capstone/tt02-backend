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
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendor_id;

    @Column(nullable = false, length = 128)
    private String business_name;

    @Column(nullable = false)
    private Date date_of_birth;

    @Column(nullable = false)
    private String poc_name;

    @Column(nullable = false)
    private String poc_position;

    @Column(nullable = false)
    private String country_code;

    @Column(nullable = false)
    private String poc_mobile_num;

//    private ApplicationStatusEnum application_status;
//
//    private VendorEnum vendor_type;

    @Column(nullable = false)
    private BigDecimal wallet_balance;

    @ManyToOne
    private Subscription subscription;

}
