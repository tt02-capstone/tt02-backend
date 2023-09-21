package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.TourType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourTypeRepository extends JpaRepository<TourType, Long> {
}