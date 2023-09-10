package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
