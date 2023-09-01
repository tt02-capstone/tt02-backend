package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristRepository extends JpaRepository<Tourist, Long> {
}
