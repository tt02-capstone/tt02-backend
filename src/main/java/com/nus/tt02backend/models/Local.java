package com.nus.tt02backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Local extends User {

    @Column(nullable = false, length = 9)
    private String nric_num;

    @Column(nullable = false)
    private Date date_of_birth;

    @Column(nullable = false)
    private String country_code;

    @Column(nullable = false)
    private String mobile_num;

    @Column(nullable = false)
    private BigDecimal wallet_balance;
}
