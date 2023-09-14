package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TouristRepository extends JpaRepository<Tourist, Long> {
    @Query("SELECT ts FROM Tourist ts WHERE ts.email=?1")
    Tourist retrieveTouristByEmail(String email);

    @Query("SELECT ts FROM Tourist ts WHERE ts.password_reset_token=?1")
    Tourist retrieveTouristByToken(String token);

}
