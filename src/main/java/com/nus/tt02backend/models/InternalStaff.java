package com.nus.tt02backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nus.tt02backend.models.enums.InternalRoleEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InternalStaff extends User {

    @Column(nullable = false, unique = true, length = 8)
    private Long staff_num;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternalRoleEnum role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "internal_staff_user")
    private List<Comment> comment_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "internal_staff_user")
    private List<Post> post_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Badge> badge_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<SupportTicket> support_ticket_list;

    @Builder
    public InternalStaff(String name, String email, String password, Boolean is_blocked, UserTypeEnum user_type, Long staff_num, InternalRoleEnum role) {
        super(name,  email,  password,  is_blocked, user_type);
        this.staff_num = staff_num;
        this.role = role;
    }

    public static class InternalStaffBuilder extends UserBuilder{
        InternalStaffBuilder() {
            super();
        }
    }
}
