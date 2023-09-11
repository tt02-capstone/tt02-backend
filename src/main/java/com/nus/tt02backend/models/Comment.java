package com.nus.tt02backend.models;


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

public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comment_id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer upvote;

    @Column(nullable = false)
    private Integer downvote;

    @Column(nullable = false)
    private LocalDateTime publish_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    @Column(nullable = false)
    @OneToMany
    private ArrayList<Comment> reply_list = new ArrayList<>();

    @OneToMany
    private ArrayList<Report> reported_comment_list = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parent_comment;

    @OneToMany(mappedBy = "parent_comment")
    private ArrayList<Comment> child_comment_list = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tourist tourist_user;

    @ManyToOne
    @JoinColumn(name = "local_id")
    private Local local_user;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private VendorStaff vendor_user;

    @ManyToOne
    @JoinColumn(name = "internal_staff_id")
    private InternalStaff internal_staff_user;
}
