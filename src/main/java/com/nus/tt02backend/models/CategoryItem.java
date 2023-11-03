package com.nus.tt02backend.models;


import jakarta.persistence.*;
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

public class CategoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_item_id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    private String image;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Post> post_list;

    @Column(nullable = false)
    private Boolean is_published;

}
