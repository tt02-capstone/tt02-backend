package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.RoomTypeEnum;
import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>  {

    @Query("SELECT b FROM Booking b WHERE b.booking_id=?1")
    Booking getBookingByBookingId(Long bookingId);

    @Query("SELECT b FROM Booking b WHERE b.type=?1")
    List<Booking> getAllTourBookings(BookingTypeEnum bookingType);


    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.start_datetime >= :startDate AND b.end_datetime <= :endDate " +
            "AND (:entityId IS NULL OR " +
            "      (b.attraction.attraction_id = :entityId AND :entityType = 'ATTRACTION') OR " +
            "      (b.room.room_id = :entityId AND :entityType = 'ACCOMMODATION') OR " +
            "      (b.telecom.telecom_id = :entityId AND :entityType = 'TELECOM')) " +
            "ORDER BY b.start_datetime")
    List<Booking> getBookingsOverTime(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("entityId") Long entityId,
            @Param("entityType") String entityType
    );


    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.start_datetime >= :startDate AND b.end_datetime <= :endDate " +
            "ORDER BY b.start_datetime")
    List<Booking> getPlatformBookingsOverTime(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
