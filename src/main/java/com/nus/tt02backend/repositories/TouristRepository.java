package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TouristRepository extends JpaRepository<Tourist, Long> {

    @Query("SELECT t.user_id FROM Tourist t WHERE t.email = ?1")
    Long getTouristIdByEmail(String email);

    @Query("SELECT t.user_id FROM Tourist t WHERE t.mobile_num = ?1")
    Long getTouristIdByMobileNum(String mobileNum);

    @Query("SELECT t.user_id FROM Tourist t WHERE t.passport_num = ?1")
    Long getTouristIdByPassportNum(String passportNum);
}
