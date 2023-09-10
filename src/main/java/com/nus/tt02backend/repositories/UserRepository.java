package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.user_id FROM User u WHERE u.email = ?1")
    Long getUserIdByEmail(String email);
}
