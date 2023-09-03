package com.nus.tt02backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long review_id;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 400)
    private String content;

    private String image;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rating;

    public Review(LocalDateTime datetime, String title, String content, BigDecimal rating) {
        this.datetime = datetime;
        this.title = title;
        this.content = content;
        this.rating = rating;
    }
}