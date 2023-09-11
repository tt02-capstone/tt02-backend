package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocalRepository extends JpaRepository<Local, Long> {

    @Query("SELECT l.user_id FROM Local l WHERE l.email = ?1")
    Long getLocalIdByEmail(String email);

    @Query("SELECT l.user_id FROM Local l WHERE l.nric_num = ?1")
    Long getLocalIdByNRICNum(String nric);

    @Query("SELECT l.user_id FROM Local l WHERE l.mobile_num = ?1")
    Long getLocalIdByMobileNum(String mobileNum);
}
