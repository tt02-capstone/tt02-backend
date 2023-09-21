package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.nus.tt02backend.models.enums.TicketEnum;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long price_id;

    @Column(nullable = false)
    private BigDecimal local_amount;

    @Column(nullable = false)
    private BigDecimal tourist_amount;

    @Enumerated(EnumType.STRING)
    private TicketEnum ticket_type;

}