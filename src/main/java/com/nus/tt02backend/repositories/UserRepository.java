package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email=?1")
    Local retrieveUserByEmail(String email);

    @Query("SELECT u FROM User u WHERE u..password_reset_token=?1")
    Local retrieveUserByToken(String token);

}
