package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.BankEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bank_account_id;

    @Enumerated(EnumType.STRING)
    private BankEnum bank_name;

    @Column(nullable = false)
    private String account_holder_name;

    @Column(nullable = false)
    private String bank_account_num;
}
