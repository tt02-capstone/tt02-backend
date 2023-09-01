package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long report_id;

    private LocalDateTime creation_date;
    private String content;

    @Column(nullable = false)
    private LocalDateTime publish_time;

//    private ReasonTypeEnum reason_type;

    @ManyToOne
    private Comment reported_comment;

    @ManyToOne
    private Post reported_post;
}
