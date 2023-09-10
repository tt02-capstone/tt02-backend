package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocalRepository extends JpaRepository<Local, Long> {
    @Query("SELECT l FROM Local l WHERE l.user_id=?1")
    Local getLocalByUserId(Long userId);
}
