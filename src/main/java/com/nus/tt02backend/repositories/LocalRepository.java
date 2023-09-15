package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocalRepository extends JpaRepository<Local, Long> {
    @Query("SELECT ls FROM Local ls WHERE ls.email=?1")
    Local retrieveLocalByEmail(String email);

    @Query("SELECT ls FROM Local ls WHERE ls.password_reset_token=?1")
    Local retrieveLocalByToken(String token);

    @Query("SELECT ls.stripe_account_id FROM Local ls WHERE ls.email=?1")
    String getStripeIdByEmail(String email);
}
