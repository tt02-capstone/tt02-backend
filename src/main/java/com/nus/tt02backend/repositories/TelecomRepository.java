package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Telecom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TelecomRepository extends JpaRepository<Telecom, Long>{
}


