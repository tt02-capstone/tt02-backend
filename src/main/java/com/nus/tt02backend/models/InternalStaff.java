package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.InternalRoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InternalStaff extends User {

    @Column(nullable = false, unique = true, length = 8)
    private String staff_num;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternalRoleEnum role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "internal_staff_user")
    private List<Comment> comment_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "internal_staff_user")
    private List<Post> post_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Badge> badge_list;
}
