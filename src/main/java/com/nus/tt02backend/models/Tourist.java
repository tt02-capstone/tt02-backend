package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tourist extends User {

    @Column(nullable = false, unique = true, length = 10)
    private String passport_num;

    @Column(nullable = false)
    private Date date_of_birth;

    @Column(nullable = false, length = 4)
    private String country_code;

    @Column(nullable = false, unique = true, length = 13)
    private String mobile_num;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Card> card_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tourist_user")
    private List<Comment> comment_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tourist_user")
    private List<Post> post_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Badge> badge_list;

    @OneToOne(fetch = FetchType.LAZY)
    private Itinerary itinerary;

    @OneToMany(fetch = FetchType.LAZY)
    private List<CartItem> cart_list;

    @OneToMany(fetch = FetchType.LAZY)
    private List<SupportTicket> support_ticket_list;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tourist_user")
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
    private List<Deal> deal_list;
}
