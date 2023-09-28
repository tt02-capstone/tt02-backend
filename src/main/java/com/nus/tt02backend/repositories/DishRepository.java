package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Dish;
import com.nus.tt02backend.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DishRepository  extends JpaRepository<Dish, Long> {
}
