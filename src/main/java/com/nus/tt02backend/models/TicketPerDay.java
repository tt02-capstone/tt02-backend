package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.TicketEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class TicketPerDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticket_per_day_id;

    private LocalDate ticket_date;

    @Column(nullable = false)
    private Integer ticket_count;

    @Enumerated(EnumType.STRING)
    private TicketEnum ticket_type;
}
