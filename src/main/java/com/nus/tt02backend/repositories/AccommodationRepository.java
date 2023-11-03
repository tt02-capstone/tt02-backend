package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Room;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.services.AccommodationService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    @Query("SELECT a FROM Accommodation a WHERE a.name=?1")
    Accommodation getAccommodationByName(String name);

    @Query("SELECT MAX(a.accommodation_id) FROM Accommodation a")
    Long findMaxAccommodationId();

    @Query("SELECT a FROM Accommodation a WHERE a.generic_location=?1")
    List<Accommodation> getAccommodationByGenericLocation(GenericLocationEnum genericLocation);

    @Query("SELECT a.address FROM Accommodation a JOIN a.room_list r WHERE r.room_id = ?1")
    String getAccomodationByRoomId(Long roomId);
}
