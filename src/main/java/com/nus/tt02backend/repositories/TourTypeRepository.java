package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.TourType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TourTypeRepository extends JpaRepository<TourType, Long> {

    @Query("SELECT MAX(t.tour_type_id) FROM TourType t")
    Long findMaxTourTypeId();
}