package com.nus.tt02backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Local extends User {

    @Column(nullable = false, unique = true, length = 9)
    private String nric_num;

    @Column(nullable = false)
    private Date date_of_birth;

    @Column(nullable = false, length = 4)
    private String country_code;

    @Column(nullable = false, unique = true, length = 13)
    private String mobile_num;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal wallet_balance;

    @Column
    private String stripe_account_id;

    @Column
    private String stripe_business_id;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Card> card_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<WithdrawBalanceRequest> withdrawal_list;

    @OneToOne(fetch = FetchType.LAZY)
    private BankAccount bank_account;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "local_user")
    private List<Comment> comment_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "local_user")
    private List<Post> post_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Badge> badge_list;

    @OneToOne(fetch = FetchType.LAZY)
    private Itinerary itinerary;

    @OneToMany(fetch = FetchType.LAZY)
    private List<CartBooking> cart_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<SupportTicket> support_ticket_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "local_user")
    private List<Booking> booking_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<TourType> tour_type_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Attraction> attraction_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Accommodation> accommodation_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Restaurant> restaurant_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Telecom> telecom_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Deal> deals_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Notification> notification_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<DIYEvent> unused_diy_event_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Item> item_list;
}
