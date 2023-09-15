package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttractionRepository extends JpaRepository<Attraction, Long>{
    @Query("SELECT a FROM Attraction a WHERE a.name=?1")
    Attraction getAttractionByName(String name);
}


