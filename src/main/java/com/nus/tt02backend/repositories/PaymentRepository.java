package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p JOIN p.booking.attraction a WHERE a.attraction_id = ?1")
    Double retrieveSumOfBookingByAttractionId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p JOIN p.booking.telecom t WHERE t.telecom_id = ?1")
    Double retrieveSumOfBookingByTelecomId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p JOIN p.booking.room r WHERE r.room_id = ?1")
    Double retrieveSumOfBookingByRoomId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p JOIN p.booking.deal d WHERE d.deal_id = ?1")
    Double retrieveSumOfBookingByDealId(Long id);
}