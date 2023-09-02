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
public class WithdrawBalanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdraw_balance_request_id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Boolean is_approved;
}
