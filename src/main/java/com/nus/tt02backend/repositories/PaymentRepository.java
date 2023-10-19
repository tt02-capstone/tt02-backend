package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND p.booking.attraction.attraction_id = ?1")
    Double retrieveSumOfBookingByAttractionId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND  p.booking.telecom.telecom_id = ?1")
    Double retrieveSumOfBookingByTelecomId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND  p.booking.room.room_id = ?1")
    Double retrieveSumOfBookingByRoomId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND  p.booking.deal.deal_id = ?1")
    Double retrieveSumOfBookingByDealId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount),0) FROM Payment p WHERE p.is_paid = true  AND p.booking.type = 'TOUR'")
    Double retrieveTourEarningsByLocalId(Long localId);
}