package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Tour;
import com.nus.tt02backend.models.TourType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TourTypeRepository extends JpaRepository<TourType, Long> {

    @Query("SELECT MAX(t.tour_type_id) FROM TourType t")
    Long findMaxTourTypeId();

    @Query("SELECT t FROM TourType t JOIN t.tour_list tl WHERE tl.tour_id=?1")
    TourType getTourTypeTiedToTour(Long tourId);

    @Query("SELECT tt FROM TourType tt WHERE tt.name = :name")
    TourType findByName(@Param("name") String name);


//    @Query("SELECT t " +
//            "FROM TourType tt " +
//            "JOIN tt.tourList t " +
//            "WHERE tt = :tourType " +
//            "AND t.date = :date " +
//            "AND t.start_time = :startTime " +
//            "AND t.end_time = :endTime")
//    Tour findTourInTourType(@Param("tourType") TourType tourType,
//                            @Param("date") LocalDateTime date,
//                            @Param("startTime") LocalDateTime startTime,
//                            @Param("endTime") LocalDateTime endTime);
}