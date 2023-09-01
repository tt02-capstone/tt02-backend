package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payment_id;
    private BigDecimal payment_amount;
    private BigDecimal comission_percentage;
    private Boolean is_paid;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;



//    private TicketEnum ticket_type;

}
