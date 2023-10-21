package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private LocalDateTime created_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_user_user_id")
    private Tourist tourist_user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_user_user_id")
    private Local local_user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_user_user_id")
    private VendorStaff vendor_staff_user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_user_user_id")
    private InternalStaff internal_staff_user;
}
