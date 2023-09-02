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

    public Price(BigDecimal local_amount, BigDecimal tourist_amount, TicketEnum ticket_type) {
        this.local_amount = local_amount;
        this.tourist_amount = tourist_amount;
        this.ticket_type = ticket_type;
    }

    public Long getPrice_id() {
        return price_id;
    }

    public void setPrice_id(Long price_id) {
        this.price_id = price_id;
    }

    public BigDecimal getLocal_amount() {
        return local_amount;
    }

    public void setLocal_amount(BigDecimal local_amount) {
        this.local_amount = local_amount;
    }

    public BigDecimal getTourist_amount() {
        return tourist_amount;
    }

    public void setTourist_amount(BigDecimal tourist_amount) {
        this.tourist_amount = tourist_amount;
    }

    public TicketEnum getTicket_type() {
        return ticket_type;
    }

    public void setTicket_type(TicketEnum ticket_type) {
        this.ticket_type = ticket_type;
    }
}