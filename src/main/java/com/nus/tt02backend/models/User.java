package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean is_blocked;

    @Enumerated(EnumType.STRING)
    private UserTypeEnum user_type;

    @Column
    private String email_verification_token;

    @Column
    private Boolean email_verified;

    @Column
    private String password_reset_token;

    @Column
    private LocalDateTime token_date;

    private LocalDateTime password_token_date;

    private String profile_pic;

    public User(String name, String email, String password, Boolean is_blocked, UserTypeEnum user_type){
        this.name = name;
        this.email = email;
        this.password = password;
        this.is_blocked = is_blocked;
        this.user_type = user_type;
    }
}
