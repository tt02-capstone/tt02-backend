package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Telecom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface TelecomRepository extends JpaRepository<Telecom, Long>{

    @Query("SELECT t FROM Telecom t WHERE t.is_published = true")
    List<Telecom> getPublishedTelecomList();
}


