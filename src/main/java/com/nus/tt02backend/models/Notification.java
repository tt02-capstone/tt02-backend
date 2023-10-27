package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.AccommodationTypeEnum;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.ListingTypeEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notification_id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 400)
    private String body;

    @Column(nullable = false)
    private Boolean is_read;

    @Column(nullable = false)
    private LocalDateTime created_datetime;
}