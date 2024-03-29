package com.nus.tt02backend.models;


import jakarta.persistence.*;
import jdk.jfr.Category;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean is_published;

    @ElementCollection
    @CollectionTable(name="post_image_list")
    private List<String> post_image_list;

    @Column(nullable = false)
    private LocalDateTime publish_time;

    @Column(nullable = false)
    private LocalDateTime updated_time;

    @ElementCollection
    @CollectionTable(name="upvoted_user_id_list")
    private List<Long> upvoted_user_id_list;

    @ElementCollection
    @CollectionTable(name="downvoted_user_id_list")
    private List<Long> downvoted_user_id_list;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    private List<Comment> comment_list;
}
