package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.SupportTicketCategoryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long support_ticket_id;

    private String description;

    private Boolean is_resolved;

    @Enumerated(EnumType.STRING)
    private SupportTicketCategoryEnum ticket_category;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Reply> reply_list;

    @OneToOne(fetch = FetchType.LAZY)
    private Booking booking;

}
