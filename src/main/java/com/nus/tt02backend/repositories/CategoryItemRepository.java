package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.CategoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryItemRepository extends JpaRepository<CategoryItem, Long>{

    @Query("SELECT MAX(ci.category_item_id) FROM CategoryItem ci")
    Long findMaxCategoryItemId();
}


