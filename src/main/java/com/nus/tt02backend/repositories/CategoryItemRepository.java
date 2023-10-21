package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Category;
import com.nus.tt02backend.models.CategoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface CategoryItemRepository extends JpaRepository<CategoryItem, Long>{

    @Query("SELECT MAX(ci.category_item_id) FROM CategoryItem ci")
    Long findMaxCategoryItemId();

    @Query("SELECT ci FROM Category c JOIN c.category_item_list ci WHERE c.category_id = ?1 AND ci.is_published = true")
    List<CategoryItem> getAllPublishedCategoryItemByCategoryId(Long categoryId);
}


