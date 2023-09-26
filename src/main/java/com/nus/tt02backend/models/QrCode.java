package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qr_code_id;

    @Column(nullable = false)
    private String voucher_code;

    @Column(nullable = false)
    private String qr_code_link;
}
