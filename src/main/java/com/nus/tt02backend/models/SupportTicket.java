package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.SupportTicketCategoryEnum;
import com.nus.tt02backend.models.enums.SupportTicketTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
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
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long support_ticket_id;

    @Column(nullable = false)
    private LocalDateTime created_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    private String description;

    private Boolean is_resolved;

    @Enumerated(EnumType.STRING)
    private SupportTicketCategoryEnum ticket_category;

    @Enumerated(EnumType.STRING)
    private SupportTicketTypeEnum ticket_type;

    @Enumerated(EnumType.STRING)
    private UserTypeEnum submitted_user;

    private Long submitted_user_id;

    private String submitted_user_name;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Reply> reply_list;

    @ManyToOne
    private Booking booking;

    @ManyToOne
    private Attraction attraction;

    @ManyToOne
    private Accommodation accommodation;

    @ManyToOne
    private Deal deal;

    @ManyToOne
    private Telecom telecom;

    @ManyToOne
    private Restaurant restaurant;

}
