package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reply_id;

    private Date date_replied;
    private String message;

    @OneToOne(fetch = FetchType.LAZY)
    private Tourist tourist_user;

    @OneToOne(fetch = FetchType.LAZY)
    private Local local_user;

    @OneToOne(fetch = FetchType.LAZY)
    private VendorStaff vendor_staff_user;

    @OneToOne(fetch = FetchType.LAZY)
    private InternalStaff internal_staff_user;
}
