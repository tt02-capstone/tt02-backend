package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long>{
}


