package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.DIYEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DIYEventRepository extends JpaRepository<DIYEvent, Long>{

    @Query("SELECT d FROM DIYEvent d WHERE d.booking IS NOT NULL AND d.start_datetime >= ?1 AND d.start_datetime <= ?2")
    List<DIYEvent> getDiyEventByDate(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT d FROM Tourist t JOIN t.unused_diy_event_list d WHERE t.user_id = ?1 AND d.booking IS NOT NULL AND d.start_datetime >= ?2 AND d.start_datetime <= ?3")
    List<DIYEvent> getDiyEventByTouristIdAndDate(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT d FROM Local l JOIN l.unused_diy_event_list d WHERE l.user_id = ?1 AND d.booking IS NOT NULL AND d.start_datetime >= ?2 AND d.start_datetime <= ?3")
    List<DIYEvent> getDiyEventByLocalIdAndDate(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}


