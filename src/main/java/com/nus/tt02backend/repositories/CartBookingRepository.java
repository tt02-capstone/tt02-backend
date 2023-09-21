package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.CartBooking;
import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartBookingRepository extends JpaRepository<CartBooking, Long>  {

    @Query("SELECT cb FROM CartBooking cb WHERE cb.cart_booking_id IN :cartBookingIds")
    List<CartBooking> findCartBookingsByIds(List<Long> cartBookingIds);

    @Query("SELECT cb FROM CartBooking cb WHERE cb.cart_booking_id = :cartBookingId")
    Optional<CartBooking> findCartBookingById(Long cartBookingId);

}