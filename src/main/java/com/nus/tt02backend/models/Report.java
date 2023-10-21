package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.ReasonTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long report_id;

    @Column(nullable = false)
    private LocalDateTime creation_date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReasonTypeEnum reason_type;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment reported_comment;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post reported_post;
}
