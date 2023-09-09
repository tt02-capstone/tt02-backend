package com.nus.tt02backend.models;


import jakarta.persistence.*;
import jdk.jfr.Category;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long post_id;

    @Column(nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(name="post_image_list")
    private ArrayList<String> post_image_list;

    @Column(nullable = false)
    private LocalDateTime publish_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    @Column(nullable = false)
    private Integer upvote;

    @Column(nullable = false)
    private Integer downvote;

    @ManyToOne
    @JoinColumn(name="category_item_id")
    private CategoryItem category_item;

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tourist tourist_user;

    @ManyToOne
    @JoinColumn(name = "local_id")
    private Local local_user;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor_user;

    @ManyToOne
    @JoinColumn(name = "internal_staff_id")
    private InternalStaff internal_staff_user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    private ArrayList<Comment> comment_list;
}
