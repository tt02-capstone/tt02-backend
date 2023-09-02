package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VendorStaff extends User {

    @Column(nullable = false)
    private Boolean is_master_account;

    @Column(nullable = false, length = 128)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vendor vendor;
}