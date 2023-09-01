package com.nus.tt02backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;


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

    private BigInteger amount;
//    private StatusEnum withdraw_balance_status;
}
