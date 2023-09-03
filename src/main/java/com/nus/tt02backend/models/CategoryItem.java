package com.nus.tt02backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
    private ArrayList<Post> post_list = new ArrayList<>();

}
