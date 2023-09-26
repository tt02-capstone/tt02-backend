package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RestaurantRepository  extends JpaRepository<Restaurant, Long> {
    @Query("SELECT r FROM Restaurant r WHERE r.name=?1")
    Restaurant getRestaurantByName(String name);


}
