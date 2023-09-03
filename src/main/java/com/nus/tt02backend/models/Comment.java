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
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comment_id;

    private Long parent_id;

//    @Column(nullable = false, unique = true)
//    private Post post:
//
//    @ManyToOne(mappedBy = "comment_list")
//    @Column(nullable = false, unique = true)
//    private Tourist user;

    @Column(nullable = false, unique = true)
    private Long upvote;

    @Column(nullable = false, unique = true)
    private Long downvote;

    private String content;

    @Column(nullable = false)
    private LocalDateTime publish_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    @Column(nullable = false)
    @OneToMany
    private ArrayList<Comment> reply_list = new ArrayList<>();

    @OneToMany
    private ArrayList<Report> reported_comment_list = new ArrayList<>();
}