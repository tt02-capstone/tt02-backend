package com.nus.tt02backend.models;


import jakarta.persistence.*;
import jdk.jfr.Category;
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
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long post_id;
//
//    @ManyToOne(mappedBy = "comment_list")
//    @Column(nullable = false, unique = true)
//    private Tourist user;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long downvote;

    @Column(nullable = false)
    private Long upvote;

    private LocalDateTime publish_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    @ManyToOne
    @JoinColumn(name="category_item_id")
    private CategoryItem category_item;

    @ElementCollection
    @CollectionTable(name="image_list")
    private ArrayList<String> image_list = new ArrayList<>();;

    @Column(nullable = false)
    @OneToMany
    private ArrayList<Report> reported_post_list = new ArrayList<>();
}
