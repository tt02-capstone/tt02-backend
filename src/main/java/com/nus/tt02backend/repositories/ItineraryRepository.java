package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Category;
import com.nus.tt02backend.models.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long>{

    @Query("SELECT i FROM Itinerary i JOIN i.diy_event_list d WHERE d.diy_event_id = ?1")
    Itinerary getItineraryContainingDiyEvent(Long diyEventId);
}


