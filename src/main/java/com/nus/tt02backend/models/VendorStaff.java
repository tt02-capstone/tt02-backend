package com.nus.tt02backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(value = {"applications", "hibernateLazyInitializer"})
public class VendorStaff extends User {

    @Column(nullable = false)
    private Boolean is_master_account;

    @Column(nullable = false, length = 128)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vendor vendor;

    @OneToMany(fetch = FetchType.LAZY)
    private List<SupportTicket> support_ticket_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vendor_staff_user")
    private List<Comment> comment_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vendor_staff_user")
    private List<Post> post_list;
}