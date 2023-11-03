package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.is_published = true ")
    List<Category> getAllPublishedCategory();

    @Query("SELECT c FROM Category c JOIN c.category_item_list ci WHERE ci.category_item_id = ?1")
    Category getCategoryContainingCategoryItem(Long categoryItemId);
}


