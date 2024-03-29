package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.BadgeTypeEnum;
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

public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badge_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeTypeEnum badge_type;

    @Column(nullable = false)
    private String badge_icon;

    @Column(nullable = false)
    private LocalDateTime creation_date;

    @Column(nullable = false)
    private Boolean is_primary;

}
