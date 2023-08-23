package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;

    @Column(nullable = false, length = 128)
    private String staffName;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(columnDefinition = "CHAR(32) NOT NULL")
    private String password;
}
