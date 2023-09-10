package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TouristRepository extends JpaRepository<Tourist, Long> {
    @Query("SELECT t FROM Tourist t WHERE t.user_id=?1")
    Tourist getTouristByUserId(Long userId);
}
