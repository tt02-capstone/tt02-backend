package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(name="comment_upvoted_user_id_list")
    private List<Long> upvoted_user_id_list;

    @ElementCollection
    @CollectionTable(name="comment_downvoted_user_id_list")
    private List<Long> downvoted_user_id_list;

    @Column(nullable = false)
    private LocalDateTime publish_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    @Column(nullable = false)
    @OneToMany
    private List<Comment> reply_list;

    @OneToMany
    private List<Report> reported_comment_list;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parent_comment;

    @OneToMany(mappedBy = "parent_comment")
    private List<Comment> child_comment_list;

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tourist tourist_user;

    @ManyToOne
    @JoinColumn(name = "local_id")
    private Local local_user;

    @ManyToOne
    @JoinColumn(name = "vendor_staff_id")
    private VendorStaff vendor_staff_user;

    @ManyToOne
    @JoinColumn(name = "internal_staff_id")
    private InternalStaff internal_staff_user;
}
