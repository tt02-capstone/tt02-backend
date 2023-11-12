package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.DIYEvent;
import com.nus.tt02backend.models.Item;
import com.nus.tt02backend.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>{

   @Query("SELECT MAX(i.item_id) FROM Item i")
   Long findMaxItemId();

   @Query("SELECT r FROM Item r WHERE r.name=?1")
   Item getItemByName(String name);
}


