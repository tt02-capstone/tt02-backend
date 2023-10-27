package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantRepository  extends JpaRepository<Restaurant, Long> {
    @Query("SELECT r FROM Restaurant r WHERE r.name=?1")
    Restaurant getRestaurantByName(String name);

    @Query("SELECT MAX(r.restaurant_id) FROM Restaurant r")
    Long findMaxRestaurantId();

    @Query("SELECT r FROM Restaurant r WHERE r.generic_location=?1")
    List<Restaurant> getRestaurantByGenericLocation(GenericLocationEnum genericLocation);
}
