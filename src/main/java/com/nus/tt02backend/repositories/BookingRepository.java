package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface BookingRepository extends JpaRepository<Booking, Long>  {

    @Query("SELECT b FROM Booking b WHERE b.booking_id=?1")
    Booking getBookingByBookingId(Long bookingId);
}
