package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TouristRepository extends JpaRepository<Tourist, Long> {

    @Query("SELECT t.stripe_id FROM Tourist t WHERE t.email=?1")
    String getStripeIdByEmail(String email);
}
