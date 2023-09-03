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
}