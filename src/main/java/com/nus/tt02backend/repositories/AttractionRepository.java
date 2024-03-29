package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long>{
    @Query("SELECT a FROM Attraction a WHERE a.name=?1")
    Attraction getAttractionByName(String name);

    @Query("SELECT MAX(a.attraction_id) FROM Attraction a")
    Long findMaxAttractionId();

    @Query("SELECT a FROM Attraction a JOIN a.tour_type_list t WHERE t.tour_type_id=?1")
    Attraction getAttractionTiedToTourType(Long tourTypeId);

    @Query("SELECT a FROM Attraction a WHERE a.suggested_duration <= ?1 AND a.is_published = true")
    List<Attraction> getAttractionsByDuration(int duration);

    @Query("SELECT a.address FROM Attraction a JOIN a.tour_type_list t JOIN t.tour_list l WHERE l.tour_id = ?1")
    String getAttractionByTourId(Long tourId);

    @Query("SELECT a FROM Attraction a WHERE a.suggested_duration<=?1")
    List<Attraction> getAttractionsByDuration(Integer duration);
}


