package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tourist extends User {

    @Column(nullable = false, length = 10)
    private String passport_num;

    @Column(nullable = false)
    private Date date_of_birth;

    @Column(nullable = false)
    private String country_code;

    @Column(nullable = false)
    private String mobile_num;
}
