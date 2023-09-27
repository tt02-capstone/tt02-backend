package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Query("SELECT d FROM Deal d WHERE d.promo_code=?1")
    Deal getDealsByPromoCode(String promocode);

    @Query("SELECT MAX(d.deal_id) FROM Deal d")
    Long findMaxDealId();
}
