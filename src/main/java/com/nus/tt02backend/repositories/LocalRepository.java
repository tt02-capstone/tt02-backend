package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocalRepository extends JpaRepository<Tourist, Long> {
    @Query("SELECT ls FROM Local ls WHERE ls.email=?1")
    Local retrieveTouristByEmail(String email);

    @Query("SELECT ls FROM Local ls WHERE ls.password_reset_token=?1")
    Local retrieveLocalByToken(String token);
}
