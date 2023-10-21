package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isPublished = true ")
    List<Category> getAllPublishedCategory();
}


