package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.models.enums.VendorEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

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

    @Column(nullable = false, length = 128)
    private String poc_name;

    @Column(nullable = false, length = 128)
    private String poc_position;

    @Column(nullable = false, length = 4)
    private String country_code;

    @Column(nullable = false, length = 13)
    private String poc_mobile_num;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal wallet_balance;

    @Enumerated(EnumType.STRING)
    private ApplicationStatusEnum application_status;

    @Enumerated(EnumType.STRING)
    private VendorEnum vendor_type;

    @Column(nullable = false)
    private String service_description;

    @OneToMany(mappedBy = "vendor")
    private List<VendorStaff> vendor_staff_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<WithdrawBalanceRequest> withdrawal_list;

    @OneToOne(fetch = FetchType.LAZY)
    private BankAccount bank_account;

    @OneToOne(fetch = FetchType.LAZY)
    private Subscription subscription;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vendor_user")
    private List<Comment> comment_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vendor_user")
    private List<Post> post_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<SupportTicket> support_ticket_list;
}
