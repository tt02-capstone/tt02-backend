package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.services.AccommodationService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    @Query("SELECT a FROM Accommodation a WHERE a.name=?1")
    Accommodation getAccommodationByName(String name);

    @Query("SELECT MAX(a.accommodation_id) FROM Accommodation a")
    Long findMaxAccommodationId();

}
