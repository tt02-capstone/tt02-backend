package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RemainingTicketPerDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long remaining_ticket_per_day_id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Integer remaining_tickets;
}
