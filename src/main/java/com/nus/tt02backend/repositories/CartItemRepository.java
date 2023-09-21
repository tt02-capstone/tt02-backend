package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>{

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart_item_id = :cartItemId")
    Optional<CartItem> findCartItemById(Long cartItemId);

}