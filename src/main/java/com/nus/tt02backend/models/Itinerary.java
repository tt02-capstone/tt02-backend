package com.nus.tt02backend.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itinerary_id;

    @Column(nullable = false)
    private LocalDateTime start_date;

    @Column(nullable = false)
    private LocalDateTime end_date;

    private Integer number_of_pax;

    @Column(nullable = false)
    private String remarks;

    @OneToMany(fetch = FetchType.LAZY)
    private List<DIYEvent> diy_event_list;

    @ElementCollection
    @CollectionTable(name="invited_people_list")
    private List<Long> invited_people_list;

    @ElementCollection
    @CollectionTable(name="accepted_people_list")
    private List<Long> accepted_people_list;

    @Column(nullable = false)
    private Long master_id;

}
